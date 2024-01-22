-- MySQL dump 10.13  Distrib 8.0.30, for Win64 (x86_64)
--
-- Host: localhost    Database: furniture_shop
-- ------------------------------------------------------
-- Server version	8.0.30

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `attributes_categories`
--

DROP TABLE IF EXISTS `attributes_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attributes_categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `attribute_id` int NOT NULL,
  `category_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ac_attributes_idx` (`attribute_id`),
  KEY `fk_ac_categories_idx` (`category_id`),
  CONSTRAINT `fk_ac_attributes` FOREIGN KEY (`attribute_id`) REFERENCES `products_attributes` (`id`),
  CONSTRAINT `fk_ac_categories` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=317 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attributes_values`
--

DROP TABLE IF EXISTS `attributes_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attributes_values` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attribute_id` int NOT NULL,
  `product_id` int NOT NULL,
  `txt_values` varchar(255) DEFAULT NULL,
  `int_value` int DEFAULT NULL,
  `float_value` float DEFAULT NULL,
  `double_value` double DEFAULT NULL,
  `bool_value` bit(1) DEFAULT NULL,
  `date_value` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_atributes_values_products_idx` (`product_id`),
  KEY `fk_attributes_values_attribute_idx` (`attribute_id`),
  KEY `product_id_idx` (`product_id`) /*!80000 INVISIBLE */,
  KEY `attribute_id_idx` (`attribute_id`),
  CONSTRAINT `fk_attributes_values_attribute` FOREIGN KEY (`attribute_id`) REFERENCES `products_attributes` (`id`),
  CONSTRAINT `fk_attributes_values_products` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2014 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Поле product_id в данной таблице ссылается именно на запись в таблице товаров, а не вариантов товаров, поскольку для разных вариантов исполнения характеристики остаются одними и теме же, меняется только представление товара (фотографии галереи и preview)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `baskets`
--

DROP TABLE IF EXISTS `baskets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `baskets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `added_date` datetime(6) DEFAULT NULL,
  `sum` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_basket_user_idx` (`user_id`),
  CONSTRAINT `fk_basket_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `baskets_products_variants`
--

DROP TABLE IF EXISTS `baskets_products_variants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `baskets_products_variants` (
  `id` int NOT NULL AUTO_INCREMENT,
  `basket_id` bigint NOT NULL,
  `product_variant_id` int NOT NULL,
  `products_count` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_bpv_baskets_idx` (`basket_id`),
  KEY `fk_bpv_variants_product_idx` (`product_variant_id`),
  CONSTRAINT `fk_bpv_baskets` FOREIGN KEY (`basket_id`) REFERENCES `baskets` (`id`),
  CONSTRAINT `fk_bpv_variants_product` FOREIGN KEY (`product_variant_id`) REFERENCES `variants_product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(255) DEFAULT NULL,
  `subcategory_id` int DEFAULT NULL,
  `parent_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_this_categories_idx` (`parent_id`),
  KEY `fk_categories_subcategories_idx` (`subcategory_id`),
  KEY `categories_id__index` (`id`),
  CONSTRAINT `fk_categories_subcategories` FOREIGN KEY (`subcategory_id`) REFERENCES `subcategories` (`id`),
  CONSTRAINT `fk_this_categories` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categories_views`
--

DROP TABLE IF EXISTS `categories_views`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories_views` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `visitor_id` int NOT NULL,
  `category_id` int NOT NULL,
  `count` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_categories_views_visitors_idx` (`visitor_id`),
  KEY `fk_categories_views_categories_idx` (`category_id`),
  CONSTRAINT `fk_categories_views_categories` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `fk_categories_views_visitors` FOREIGN KEY (`visitor_id`) REFERENCES `visitors` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `surname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `patronymic` varchar(255) DEFAULT NULL,
  `phone_number` decimal(17,0) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `visitor_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_customers_visitors_idx` (`visitor_id`),
  CONSTRAINT `fk_customers_visitors` FOREIGN KEY (`visitor_id`) REFERENCES `visitors` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `daily_visits`
--

DROP TABLE IF EXISTS `daily_visits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_visits` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `count` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `date_UNIQUE` (`date`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_states`
--

DROP TABLE IF EXISTS `order_states`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_states` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_state` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_state_id` int NOT NULL,
  `customer_id` int NOT NULL,
  `code` bigint NOT NULL COMMENT 'Номер заказа для получения пользователю информации о нём',
  `order_date` datetime(6) DEFAULT NULL,
  `sum` int DEFAULT '0' COMMENT 'Итоговая сумма заказа',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`),
  KEY `fk_orders_order_states_idx` (`order_state_id`),
  KEY `fk_orders_customers_idx` (`customer_id`),
  CONSTRAINT `fk_orders_customers` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `fk_orders_order_states` FOREIGN KEY (`order_state_id`) REFERENCES `order_states` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orders_products_variants`
--

DROP TABLE IF EXISTS `orders_products_variants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders_products_variants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_variant_id` int NOT NULL,
  `order_id` bigint NOT NULL,
  `products_count` int DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_product_variants_idx` (`product_variant_id`),
  KEY `fk_orders_idx` (`order_id`),
  CONSTRAINT `fk_orders` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_product_variants` FOREIGN KEY (`product_variant_id`) REFERENCES `variants_product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `producers`
--

DROP TABLE IF EXISTS `producers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `producer_name` varchar(255) DEFAULT NULL,
  `deleted_at` date DEFAULT NULL COMMENT 'Поле для осуществления мягкого удаления',
  `is_shown` bit(1) NOT NULL DEFAULT b'1' COMMENT 'Выводятся ли товары и сам производитель в результатх выборок',
  `producer_img` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `producers_id_index` (`id`),
  FULLTEXT KEY `full_text_name_idx` (`producer_name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_name` varchar(255) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `category_id` int NOT NULL,
  `producer_id` int NOT NULL,
  `is_available` bit(1) DEFAULT NULL,
  `show_product` bit(1) DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT b'0',
  `mark` float DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_products_categories_idx` (`category_id`),
  KEY `fk_products_producers_idx` (`producer_id`),
  FULLTEXT KEY `full_text_search_idx` (`product_name`,`description`),
  CONSTRAINT `fk_products_categories` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `fk_products_producers` FOREIGN KEY (`producer_id`) REFERENCES `producers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products_attributes`
--

DROP TABLE IF EXISTS `products_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products_attributes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `attr_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `attribute_name_unique` (`attr_name`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products_images`
--

DROP TABLE IF EXISTS `products_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_variant_id` int NOT NULL,
  `img_link` varchar(255) NOT NULL,
  `img_order` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_products_images_products_idx` (`id`,`product_variant_id`),
  KEY `fk_products_images_product_variants_idx` (`product_variant_id`),
  CONSTRAINT `fk_products_images_product_variants` FOREIGN KEY (`product_variant_id`) REFERENCES `variants_product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products_prices`
--

DROP TABLE IF EXISTS `products_prices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products_prices` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_variant_id` int NOT NULL,
  `price` int NOT NULL,
  `date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_prices_product_variants_idx` (`product_variant_id`),
  CONSTRAINT `fk_prices_product_variants` FOREIGN KEY (`product_variant_id`) REFERENCES `variants_product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products_views`
--

DROP TABLE IF EXISTS `products_views`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products_views` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `visitor_id` int NOT NULL,
  `product_id` int NOT NULL,
  `count` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_products_views_visitors_idx` (`visitor_id`),
  KEY `fk_products_views_products_idx` (`product_id`),
  CONSTRAINT `fk_products_views_products` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_products_views_visitors` FOREIGN KEY (`visitor_id`) REFERENCES `visitors` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Сдесь считаются именно просмотры товаров, а не их вариантов исполнения. Это сделано осознанно!';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subcategories`
--

DROP TABLE IF EXISTS `subcategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subcategories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sub_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `subcategories_id_index` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `login` varchar(255) NOT NULL,
  `email` varchar(70) NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `login_UNIQUE` (`login`),
  KEY `fk_users_roles_idx` (`role_id`),
  CONSTRAINT `fk_users_roles` FOREIGN KEY (`role_id`) REFERENCES `user_roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users_passwords`
--

DROP TABLE IF EXISTS `users_passwords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_passwords` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variants_product`
--

DROP TABLE IF EXISTS `variants_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `variants_product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `preview_img` varchar(255) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `price` int NOT NULL,
  `show_variant` bit(1) DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `fk_products_variants_idx` (`product_id`),
  KEY `product_id_index` (`product_id`),
  KEY `variant_id_index` (`id`),
  FULLTEXT KEY `fulltext_title_index` (`title`),
  CONSTRAINT `fk_products_variants` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=189 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Правильнм назвзанием было бы: product_variants, но таблиц с префиксом product и так слишком много, так что данную таблицу будет проще найти при таком именовании)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `view_categories`
--

DROP TABLE IF EXISTS `view_categories`;
/*!50001 DROP VIEW IF EXISTS `view_categories`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `view_categories` AS SELECT 
 1 AS `id`,
 1 AS `name`,
 1 AS `subcategory_name`,
 1 AS `parent_category`,
 1 AS `parent_id`,
 1 AS `subcategory_id`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `view_products`
--

DROP TABLE IF EXISTS `view_products`;
/*!50001 DROP VIEW IF EXISTS `view_products`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `view_products` AS SELECT 
 1 AS `id`,
 1 AS `product_name`,
 1 AS `description`,
 1 AS `category`,
 1 AS `parent_category`,
 1 AS `producer_name`,
 1 AS `is_available`,
 1 AS `show_product`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `view_products_features`
--

DROP TABLE IF EXISTS `view_products_features`;
/*!50001 DROP VIEW IF EXISTS `view_products_features`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `view_products_features` AS SELECT 
 1 AS `product_id`,
 1 AS `product_name`,
 1 AS `description`,
 1 AS `category`,
 1 AS `parent_category`,
 1 AS `producer_name`,
 1 AS `is_available`,
 1 AS `show_product`,
 1 AS `feature`,
 1 AS `feature_value`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `visitors`
--

DROP TABLE IF EXISTS `visitors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitors` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ip_address` varchar(255) DEFAULT NULL,
  `fingerprint` varchar(255) DEFAULT NULL,
  `created_at` date DEFAULT NULL,
  `last_visit_at` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Final view structure for view `view_categories`
--

/*!50001 DROP VIEW IF EXISTS `view_categories`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `view_categories` AS select `categories`.`id` AS `id`,ifnull(`categories`.`category_name`,'---') AS `name`,ifnull(`subcategories`.`sub_name`,'---') AS `subcategory_name`,`parent_category`.`category_name` AS `parent_category`,`categories`.`parent_id` AS `parent_id`,`subcategories`.`id` AS `subcategory_id` from ((`categories` left join `subcategories` on((`categories`.`subcategory_id` = `subcategories`.`id`))) join `categories` `parent_category` on((`categories`.`parent_id` = `parent_category`.`id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `view_products`
--

/*!50001 DROP VIEW IF EXISTS `view_products`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `view_products` AS select `products`.`id` AS `id`,`products`.`product_name` AS `product_name`,`products`.`description` AS `description`,ifnull(`categories`.`category_name`,`s`.`sub_name`) AS `category`,(select `cat`.`category_name` from `categories` `cat` where (`cat`.`id` = `categories`.`parent_id`)) AS `parent_category`,`producers`.`producer_name` AS `producer_name`,if((`products`.`is_available` = 1),'true','false') AS `is_available`,if((`products`.`show_product` = 1),'true','false') AS `show_product` from ((`products` join (`categories` left join `subcategories` `s` on((`categories`.`subcategory_id` = `s`.`id`))) on((`products`.`category_id` = `categories`.`id`))) join `producers` on((`products`.`producer_id` = `producers`.`id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `view_products_features`
--

/*!50001 DROP VIEW IF EXISTS `view_products_features`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `view_products_features` AS select `products`.`id` AS `product_id`,`products`.`product_name` AS `product_name`,`products`.`description` AS `description`,ifnull(`categories`.`category_name`,`s`.`sub_name`) AS `category`,(select `cat`.`category_name` from `categories` `cat` where (`cat`.`id` = `categories`.`parent_id`)) AS `parent_category`,`producers`.`producer_name` AS `producer_name`,if((`products`.`is_available` = 1),'true','false') AS `is_available`,if((`products`.`show_product` = 1),'true','false') AS `show_product`,`p_attr`.`attr_name` AS `feature`,ifnull(`av`.`txt_values`,ifnull(`av`.`int_value`,ifnull(`av`.`float_value`,ifnull(`av`.`double_value`,ifnull(`av`.`bool_value`,`av`.`date_value`))))) AS `feature_value` from (((`products` join (`categories` left join `subcategories` `s` on((`categories`.`subcategory_id` = `s`.`id`))) on((`products`.`category_id` = `categories`.`id`))) join `producers` on((`products`.`producer_id` = `producers`.`id`))) join (`attributes_values` `av` join `products_attributes` `p_attr` on((`av`.`attribute_id` = `p_attr`.`id`))) on((`av`.`product_id` = `products`.`id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-01-22 12:36:04
