// Модель корзины
export class Basket {

    // Список объектов id вариантов товаров
    pvCartItems = [];
    generalCount = 0;

    constructor({pvItems, count}) {

        this.pvCartItems = pvItems;
        this.generalCount = count;
    }


}