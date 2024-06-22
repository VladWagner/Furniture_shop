export class CategoryDto {

    constructor({id, categoryName, parentCategoryId = null,  productsAmount, image = null}) {
        this.id = id;
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId || 0;
        this.productsAmount = productsAmount;
        this.img = image;
    }


    static readFromParsedObject(category){

        return new CategoryDto({
            id: category.id,
            categoryName: category.category_name,
            parentCategoryId: category.parent_category_id,
            productsAmount: category.products_amount,
            image: category.image ? category.image : null
        });

    }
}
