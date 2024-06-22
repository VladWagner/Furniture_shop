package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entities.users.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRolesRepository extends JpaRepository<UserRole,Long> {

    // Получить роль с минимальными правами
    @Query(value = """
    select
    ur
    from
        UserRole ur
    where
        ur.role like concat('%','окупатель','%') or ur.role like concat('%','ользователь','%')
""")
    UserRole getBasicRole();


    //Получить maxId
    @Query(value = """
    select
        max(u.id)
    from
        User u
    """)
    long getMaxId();
}
