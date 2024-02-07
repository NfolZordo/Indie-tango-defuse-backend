package indie.tango.defuse.repositoriy;

import indie.tango.defuse.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);
  Optional<User> findById(Integer Id);

  @Modifying
  @Query(value =
          "select * from all_user_info " +
                  "where email like :email", nativeQuery = true)
  Object[] getUserInfo(
          @Param("email") String email
  );

  @Modifying
  @Query(value =
  "INSERT INTO public._user( " +
          "email, password, role) " +
          "VALUES (:email, :password, 'USER');", nativeQuery = true)
  void saveUser(
          @Param("email") String email,
          @Param("password") String password
  );

  @Query(value =
          "SELECT uf.email " +
                  "FROM public.friend f " +
                  "JOIN _user u ON u.id = f.user_id " +
                  "JOIN _user uf ON uf.id = f.friend_id " +
                  "WHERE u.email = :email", nativeQuery = true)
  List<String> getFriends(@Param("email") String email);

  @Modifying
  @Query(value =
        "INSERT INTO public.friend( " +
                "user_id, friend_id) " +
                "VALUES (:email, :friendEmail);", nativeQuery = true)
  void addFriends(@Param("email") int email,
                  @Param("friendEmail") int friendEmail);
}
