package gp.wagner.backend.controllers.admin_panel;

import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.middleware.Services;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin_panel")
public class AdminPanelController {

    // Скрыть товар
    @GetMapping(value = "/hide_product/{product_id}")
    public ResponseEntity<Boolean> hideProductById(@PathVariable(value = "product_id") long productId) throws ApiException {

        Services.productsService.hideById(productId);

        return ResponseEntity.ok().body(true);
    }

    // Восстановить из скрытия товар
    @GetMapping(value = "/recover_hidden_product")
    public ResponseEntity<Boolean> hideProductById(@RequestParam(value = "product_id") long productId,
                                                   @RequestParam(value = "recover_heirs") boolean recoverHeirs) throws ApiException {

        Services.productsService.recoverHiddenById(productId, recoverHeirs);

        return ResponseEntity.ok().body(true);
    }

    // Скрыть производителя и все его товары
    @GetMapping(value = "/hide_producer/{producer_id}")
    public ResponseEntity<Boolean> hideProducerById(@PathVariable(value = "producer_id") long producerId) throws ApiException {

        Services.producersService.hideById(producerId);

        return ResponseEntity.ok().body(true);
    }

    // Восстановить из скрытия производителя и все его товары
    @GetMapping(value = "/recover_hidden_producer")
    public ResponseEntity<Boolean> hideProducerById(@RequestParam(value = "producer_id") long producerId,
                                                   @RequestParam(value = "recover_heirs") boolean recoverHeirs) throws ApiException {

        Services.producersService.recoverHiddenById(producerId, recoverHeirs);

        return ResponseEntity.ok().body(true);
    }

    // Скрыть категорию и все товары в ней
    @GetMapping(value = "/hide_category/{category_id}")
    public ResponseEntity<Boolean> hideCategoryById(@PathVariable(value = "category_id") long categoryId) throws ApiException {

        Services.categoriesService.hideById(categoryId);

        return ResponseEntity.ok().body(true);
    }

    // Восстановить из скрытия производителя и все его товары
    @GetMapping(value = "/recover_hidden_category")
    public ResponseEntity<Boolean> hideCategoryById(@RequestParam(value = "category_id") long producerId,
                                                   @RequestParam(value = "recover_heirs") boolean recoverHeirs) throws ApiException {

        Services.categoriesService.recoverHiddenById(producerId, recoverHeirs);

        return ResponseEntity.ok().body(true);
    }

    // Изменить роль пользователя (данный endpoint доступен только админу)
    @PutMapping(value = "/change_user_role")
    public ResponseEntity<Boolean> changeUserRole(@RequestParam(value = "user_id") long userId,
                                                    @RequestParam(value = "role_id") int roleId) throws ApiException {

        Services.usersService.changeRole(userId, roleId);

        return ResponseEntity.ok().body(true);
    }

}
