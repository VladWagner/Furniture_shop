-- Триггер для создания даты добавления стоимости варианта товара
delimiter ;;
create trigger product_price_timestamp_trigger before INSERT on products_prices for each row
    begin
        set new.date = now();
    end ;;
delimiter ;

-- Триггер для создания даты добавления записи в таблицу заказов
delimiter ;;
create trigger order_timestamp_trigger before INSERT on orders for each row
    begin
        set new.order_date = now();
    end ;;
delimiter ;

-- Триггер для зада даты добавления товара в таблицу корзины
delimiter ;;
create trigger basket_timestamp_trigger before INSERT on baskets for each row
    begin
        set new.added_date = now();
    end ;;
delimiter ;