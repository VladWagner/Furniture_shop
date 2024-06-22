import {useDispatch} from "react-redux";
import {bindActionCreators} from "redux";
import actionsCreator from "../store/actionsCreator";

export const useActions = () => {
  let dispatch = useDispatch();

  return bindActionCreators(actionsCreator, dispatch)
}