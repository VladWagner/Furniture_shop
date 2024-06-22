import {instance} from "../../infrastrucutre/axiosInterceptor";
import {ProductVariantPreviewDto} from "../dto/request/productVariantPreviewDto";

export const ProductsVariantsApi = () => ({
    async getByIdsList(idsList) {

        if (!idsList)
            return null;

        return instance.get("/api/product_variants/variant_by_ids_list", {
            params: "ids_list",
            paramsSerializer: params => `ids_list=${idsList.join(',')}`
        }).then(resp => {

            let pvDtoList = resp.data.map(pv => ProductVariantPreviewDto.readParsedObject(pv));

            return pvDtoList;

        }).catch(error => {

            console.dir(error);

            if (!error || !error.response)
                return Promise.reject("Unknown error")

            console.log(error.response.data.message);

            return Promise.reject(error.response.data.message);
        })
    }
})