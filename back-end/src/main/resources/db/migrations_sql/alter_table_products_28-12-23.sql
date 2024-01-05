/*Добавить поле оценки товара и флаг удалён ли он или нет*/
ALTER TABLE `furniture_shop`.`products`
    ADD COLUMN `is_deleted` BIT(1) NULL DEFAULT NULL AFTER `show_product`,
ADD COLUMN `mark` FLOAT NULL DEFAULT NULL AFTER `is_deleted`;
