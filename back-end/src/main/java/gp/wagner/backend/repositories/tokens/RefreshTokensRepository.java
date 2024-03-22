package gp.wagner.backend.repositories.tokens;

import gp.wagner.backend.domain.entites.tokens.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface RefreshTokensRepository extends JpaRepository<RefreshToken,Long> {

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
    insert into refresh_tokens
        (token, user_id, expires_at)
    values
        (:token, :user_id, :expiration_date)
""")
    void insert(@Param("token") String token, @Param("user_id") long id, @Param("expiration_date") Date expiresAt);

    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserId(long userId);

    // Удаление токенов с датой <= заданной
    void deleteRefreshTokenByExpiresAtLessThanEqual(Date date);

    // Удалить все токены с истёкшим сроком по отношению к текущему моменту
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
    delete from refresh_tokens rt
    where rt.expires_at <= :curr_date
    """)
    void deleteExpiredTokens(@Param("curr_date") Date now);
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
    delete from refresh_tokens rt
    where rt.expires_at <= now()
    """)
    void deleteExpiredTokens();

    // Удаление определённого токена если он просрочен
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
    delete from refresh_tokens rt
    where rt.expires_at <= now() and rt.token = :token
    """)
    void deleteExpiredToken(@Param("token") String token);

    // Удаление токенов для определённого пользователя - в случае отзыва токенов
    void deleteAllByUserId(long userId);

    @Query(value = """
    select
        rt
    from
        RefreshToken rt
    where rt.token = :token and ((:user_id is not null and rt.user.id = :user_id) or :user_id is null)
""")
    Optional<RefreshToken> findByTokenAndUser(@Param("token") String token, @Param("user_id") Long userId);

}
