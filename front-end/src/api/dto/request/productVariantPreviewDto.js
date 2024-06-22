// DTO предосмотра варианта товара отправляемое с бэка
export class ProductVariantPreviewDto{
    constructor({id, productId, productName, title, previewImgLink, price, discountPrice, discountPercent, isShown, isDeleted}) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.title = title;
        this.previewImgLink = previewImgLink;
        this.price = price;
        this.discountPrice = discountPrice;
        this.discountPercent = discountPercent;
        this.showVariant = isShown;
        this.isDeleted = isDeleted;
    }

    static readParsedObject(pv){
        return new ProductVariantPreviewDto({
            id: pv.id,
            productId: pv.productId,
            productName: pv.productName,
            title: pv.title,
            previewImgLink: pv.previewImgLink,
            price: pv.price,
            isShown: pv.showVariant,
            isDeleted: pv.deleted,
            discountPrice: pv.discount_price,
            discountPercent: pv.discount_percent
        })
    }
}