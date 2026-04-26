function copyToClipboard(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.select();
        document.execCommand('copy');
    }
}
function toutEffacer() {
    document.getElementById('question').value = '';
    document.getElementById('reponse').value = '';
}
