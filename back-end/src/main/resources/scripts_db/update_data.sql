-- Задать значения кодов заказов
update orders
set orders.code = RAND()*(10000000000-10000000)+10000000
where orders.id < 15;
