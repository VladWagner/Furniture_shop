package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.Visitor;
import jakarta.annotation.Nullable;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface VisitorsRepository extends JpaRepository<Visitor,Long> {

    //Добавление записи о посетителе
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into visitors  
        (fingerprint, ip_address)
        values 
        (:fp, :ip)
    """)
    int insertVisitor(@Param("ip") String ip, @Param("fp") String fingerPrint);

    //Изменение посетителя
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update visitors set
                        ip_address = :ip,fingerprint = :fp
    where id = :visitorId
    """)
    void updateVisitor(@Param("visitorId") long id, @Param("ip") @DefaultValue(" ") String ip, @Param("fp") String fingerPrint);

    //Получить посетителя по finger print
    Visitor getVisitorByFingerprint(String fingerPrint);

    //Получить maxId
    @Query(value = """
    select
        max(v.id)
    from
        Visitor v
    """)
    long getMaxId();
}
