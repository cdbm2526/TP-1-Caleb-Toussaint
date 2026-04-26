package ht.mbds.calebtoussaint.tp1_caleb_toussaint.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * Backing bean pour la page JSF index.xhtml.
 * Porte view pour conserver l'tat de la conversation qui dure pendant plusieurs requtes HTTP.
 * La porte view ncessite l'implmentation de Serializable (le backing bean peut tre mis en mmoire secondaire).
 */
@Named
@ViewScoped
public class Bb implements Serializable {
    /**
     * Rle "systme" que l'on attribuera plus tard  un LLM.
     * Possible d'crire un nouveau rle dans la liste droulante.
     */
    private String roleSysteme;
    /**
     * Quand le rle est choisi par l'utilisateur dans la liste droulante,
     * il n'est plus possible de le modifier (voir code de la page JSF), sauf si on veut un nouveau chat.
     */
    private boolean roleSystemeChangeable = true;
    /**
     * Liste de tous les rles de l'API prdfinis.
     */
    private List<SelectItem> listeRolesSysteme;
    /**
     * Dernire question pose par l'utilisateur.
     */
    private String question;
    /**
     * Dernire rponse de l'API OpenAI.
     */
    private String reponse = "";
    /**
     * La conversation depuis le dbut.
     */
    private StringBuilder conversation = new StringBuilder();
    /**
     * Service pour modifier la question et gnrer la rponse.
     */
    @Inject
    private Modificateur modificateur;
    /**
     * Contexte JSF. Utilis pour qu'un message d'erreur s'affiche dans le formulaire.
     */
    @Inject
    private FacesContext facesContext;
    /**
     * Obligatoire pour un bean CDI (classe gre par CDI), s'il y a un autre constructeur.
     */
    public Bb() {
    }
    public String getRoleSysteme() {
        return roleSysteme;
    }
    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }
    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }
    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    public String getReponse() {
        return reponse;
    }
    /**
     * setter indispensable pour le textarea.
     *
     * @param reponse la rponse  la question.
     */
    public void setReponse(String reponse) {
        this.reponse = reponse;
    }
    public String getConversation() {
        return conversation.toString();
    }
    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }
    /**
     * Envoie la question au serveur.
     * En attendant de l'envoyer  un LLM, le serveur fait un traitement quelconque, juste pour tester :
     * Le traitement consiste  copier la question en minuscules et  l'entourer avec "||". Le rle systme
     * est ajout au dbut de la premire rponse.
     *
     * @return null pour rester sur la mme page.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            // Erreur ! Le formulaire va tre raffich en rponse  la requte POST, avec un message d'erreur.
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }
        // Traite la question pour construire la rponse.
        String roleSystemePourModification = null;
        if (this.conversation.isEmpty()) { // Si la conversation n'a pas encore commenc
            roleSystemePourModification = this.roleSysteme; // Pour Modificateur.modifier()
            // Invalide la liste pour changer le rle systme
            this.roleSystemeChangeable = false;
        }
        this.reponse += this.modificateur.modifier(this.question, roleSystemePourModification);
        // La conversation contient l'historique des questions-rponses depuis le dbut.
        afficherConversation();
        return null;
    }
    /**
     * Pour un nouveau chat.
     * Termine la porte view en retournant "index" (la page index.xhtml sera affiche aprs le traitement
     * effectu pour construire la rponse) et pas null. null aurait indiqu de rester dans la mme page (index.xhtml)
     * sans changer de vue.
     * Le fait de changer de vue va faire supprimer l'instance en cours du backing bean par CDI et donc on reprend
     * tout comme au dbut puisqu'une nouvelle instance du backing va tre utilise par la page index.xhtml.
     * @return "index"
     */
    public String nouveauChat() {
        return "index";
    }
    /**
     * Pour afficher la conversation dans le textArea de la page JSF.
     */
    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question).append("\n== Serveur:\n").append(reponse).append("\n");
    }
    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            // Gnre les rles de l'API prdfinis
            this.listeRolesSysteme = new ArrayList<>();
            // Vous pouvez videmment crire ces rles dans la langue que vous voulez.
            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            // 1er argument : la valeur du rle, 2me argument : le libell du rle
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));
            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Franais"));
            role = """
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    are you tell them the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
        }
        return this.listeRolesSysteme;
    }
}
