import {useDispatch} from "react-redux";
import {bindActionCreators} from "redux";
import actionsCreator from "../store/actionsCreator";
import {createToast} from "../store/actions/toastNotificationsActions";
import {useActions} from "./useActions";
import {toastsTypes} from "../infrastrucutre/constants";

export const useToastActions = () => {
  const {createToast} = useActions();

  const createSuccessToast = function (message, timeout = 0) {

    createToast(message, toastsTypes.SUCCESS, timeout)
  };

  const createWarningToast = function (message, timeout = 0) {
    createToast(message, toastsTypes.WARNING, timeout)
  };

  const createMessageToast = function (message, timeout = 0) {
    createToast(message, toastsTypes.MESSAGE, timeout)
  };

  const createErrorToast = function (message, timeout = 0) {
    createToast(message, toastsTypes.ERROR, timeout)
  };

  return {
    createSuccessToast,
    createErrorToast,
    createMessageToast,
    createWarningToast,
  }
}