export class CategoryWithChildrenDto {

    /*constructor(category) {
        this.id = category.id;
        this.categoryName = category.category_name;
        this.parentCategoryId = category.parent_category_id || 0;
        this.childCategories = category.child_categories ? category.child_categories
            .map(child => new CategoryWithChildrenDto(child)) : [];
        this.productsAmount = category.products_amount;
    }*/

    constructor({id, categoryName, parentCategoryId, childCategories, productsAmount, isParsedObject}) {
        this.id = id;
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId || 0;
        this.childCategories = childCategories ? childCategories
            .map(child => isParsedObject ? CategoryWithChildrenDto.readFromParsedObject(child) : CategoryWithChildrenDto.readJsObject(child)) : [];
        this.productsAmount = productsAmount;
    }


    static readJsObject(category){

        return  new CategoryWithChildrenDto({
            id: category.id,
            categoryName: category.categoryName,
            parentCategoryId: category.parentCategoryId,
            childCategories: category.childCategories,
            productsAmount: category.productsAmount,
            isParsedObject: false
        });

    }

    static readFromParsedObject(category){

        return  new CategoryWithChildrenDto({
            id: category.id,
            categoryName: category.category_name,
            parentCategoryId: category.parent_category_id,
            childCategories: category.child_categories,
            productsAmount: category.products_amount,
            isParsedObject: true
        });

    }
}
