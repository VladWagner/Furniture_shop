import {ProductVariantPreviewDto} from "./productVariantPreviewDto";

// Объект контейнер для DTO варианта товара и его кол-ва в корзине
export class ProductVariantCount {
    constructor({productVariant, count}) {
        this.productVariantDto = productVariant;
        this.count = count;
    }

    static readFromParsedObj(bpv){
        return new ProductVariantCount({
            productVariant: ProductVariantPreviewDto.readParsedObject(bpv.productVariantDto),
            count: bpv.count
        }
        );
    }

}

// DTO корзины вместе с вариантами товаров
export class BasketDto {
    constructor(basket) {
        this.id = basket.id;
        this.productVariantsAndCount = basket.productVariants.map(bpv => ProductVariantCount.readFromParsedObj(bpv));
        this.sum = basket.sum;
        this.addingDate = basket.addingDate;
        this.userId = basket.userId;
        this.userName = basket.userName;
    }
}