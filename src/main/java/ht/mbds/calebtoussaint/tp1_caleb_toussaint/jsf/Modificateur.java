package ht.mbds.calebtoussaint.tp1_caleb_toussaint.jsf;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Modificateur {

    public String modifier(String question, String roleSysteme) {
        String reponse = "";
        if (roleSysteme != null) {
            reponse += "[Rôle: " + roleSysteme + "]\n";
        }
        reponse += "||" + question.toLowerCase() + "||";
        return reponse;
    }
}