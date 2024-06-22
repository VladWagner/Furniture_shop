package gp.wagner.backend.security.configurations;


import gp.wagner.backend.security.filters.AuthExceptionsHandlingFilter;
import gp.wagner.backend.security.filters.JwtAuthorizationFilter;
import gp.wagner.backend.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static gp.wagner.backend.infrastructure.enums.UsersRolesEnum.ADMIN;
import static gp.wagner.backend.infrastructure.enums.UsersRolesEnum.EDITOR;
import static org.springframework.http.HttpMethod.*;

@Configuration
public class SecurityConfiguration {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;

    @Autowired
    private CorsConfig corsConfig;

    // Список маршрутов, для которых авторизация не нужна
    private static final String[] WHITE_LIST = new String[]{
            "/api/auth/*",
            "/api/search/*",
            "/api/products/**",

            "/api/users/resend_confirmation",
            "/api/users/confirm",
            "/api/users/check_login",
            "/api/users/check_email",

            "/api/orders/order_by_code/*",
            "/api/orders/get_payment_methods",
            "/api/orders/create_order",

            "/api/products_reviews/by_product",
            "/api/product_variants/**",

            "/api/categories/",
            "/api/categories/get_all_with_repeating",
            "/api/categories/get_tree",
            "/api/categories/breadcrumbs/**",

            "/api/producers",
            "/api/producers/all",
            "/api/products_ratings/**",
            "/api/filter/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        return new UserDetailsServiceImpl();
    }

    // Задать ограничения доступа на маршруты
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        OncePerRequestFilter authorizationFilter = new JwtAuthorizationFilter(exceptionResolver);
        http.authorizeHttpRequests(
                        authorize ->
                                authorize.requestMatchers(WHITE_LIST)
                                        .permitAll()
                                        .requestMatchers("/api/users/**").authenticated()
                                        .requestMatchers("/api/users/all").hasAuthority(ADMIN.getRoleName())
                                        .requestMatchers("/api/users/change_role/*").hasAuthority(ADMIN.getRoleName())

                                        .requestMatchers(POST,"/api/products/*").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers(PUT,"/api/products/*").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers(DELETE,"/api/products/*").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())

                                        .requestMatchers(POST,"/api/product_variants/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers(PUT,"/api/product_variants/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers(DELETE,"/api/product_variants/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())

                                        .requestMatchers("/api/admin_panel/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/admin_panel/stat/**", "/api/admin_panel/stat").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())

                                        .requestMatchers(GET,"/api/baskets").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/baskets/by_product_variant/*", "/api/baskets/user/*").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/baskets/**", "/api/baskets").authenticated()

                                        .requestMatchers("/api/discounts/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())

                                        .requestMatchers(POST,"/api/categories").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers(POST,"/api/categories/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers(PUT,"/api/categories/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers(DELETE,"/api/categories/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/categories/category_views/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())

                                        .requestMatchers(POST,"/api/producers/**", "/api/producers").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers(PUT,"/api/producers/**", "/api/producers").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers(DELETE,"/api/producers/**", "/api/producers").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/producers/all_deleted").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())

                                        .requestMatchers("/api/products_reviews/verify/**", "api/products_reviews/all").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/products_reviews/add_review", "api/products_reviews/update_review").authenticated()

                                        .requestMatchers("/api/products_ratings/all").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())

                                        .requestMatchers("/api/stat/daily_visits/increase_counter").permitAll()
                                        .requestMatchers("/api/stat/daily_visits/**", "/api/stat/daily_visits").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())

                                        .requestMatchers("/api/orders").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/orders/create_payment_method", "/api/orders/get_all_customers").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/orders/update_status","/api/orders/cancel_order/**").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/orders/delete_by_code","/api/orders/orders_dates_range","/api/orders/recount_sums").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/orders/orders_by_pv","/api/orders/orders_for_product").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                                        .requestMatchers("/api/orders/orders_for_user", "/api/orders/orders_for_customer").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())

                                        .requestMatchers("/api/filter/filter_for_customers").hasAnyAuthority(ADMIN.getRoleName(), EDITOR.getRoleName())
                )
                .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new AuthExceptionsHandlingFilter(exceptionResolver), ExceptionTranslationFilter.class);

        return http.build();
    }
}
