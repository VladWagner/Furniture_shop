export class Constants {

    static localIp = "192.168.0.105";
    static localAppAddr = ":8080/furniture_shop_backend";
    //static localAppAddr = ":8080/";

    static tomcatUploadsRoute = "http://" + Constants.localIp + ":3001/tomcat";
    static driveUploadsRoute = "http://" + Constants.localIp + ":3001/drive";
    static staticFilesUploadsRoute = "http://" + Constants.localIp + ":3001/static";

    static errorFormField = "form_field_error";
    static messageFormField = "form_field_message";
    static validFormField = "form_field_valid";

    static regExpEmail = new RegExp("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    static regExpUserName = new RegExp("^[a-zA-Z0-9._-]{3,}$");
    static regExpPassword = {
        minLength: new RegExp(".{8,}"),
        letters: new RegExp("[a-zA-Z]+"),
        digits: new RegExp(".*[0-9].*")
    };

    static syncBasketLsKey = "synchronize_basket_server";
    static sendBasketToServerLsKey = "send_basket_to_server";

}

export const toastsTypes = {
    SUCCESS: "success",
    WARNING: "warning",
    MESSAGE: "message",
    ERROR: "error"
}
export const toastsContainerPositions = {
    TOP: "top",
    BOTTOM: "bottom"
}