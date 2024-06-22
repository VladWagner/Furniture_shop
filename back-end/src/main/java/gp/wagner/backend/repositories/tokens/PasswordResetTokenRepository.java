package gp.wagner.backend.repositories.tokens;

import gp.wagner.backend.domain.entities.tokens.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {

    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserId(long userId);

    // Удаление токенов с датой <= заданной
    void deleteByExpiryDateLessThan(Date date);

    // Удалить все токены с истёкшим сроком по отношению к текущему момент
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
    delete from password_reset_token prt
    where prt.expiry_date <= :curr_date
    """)
    void deleteExpiredTokens(@Param("curr_date") Date now);

}
