package org.example.budgetservice.repo;

import org.example.budgetservice.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserId(String userId);

    boolean existsByUserId(String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Budget b
                set b.currentSpent = :total,
                    b.updatedAt = CURRENT_TIMESTAMP 
            where b.userId = :userId        
           """)
    int updateCurrentSpent(@Param("userId") String userId, @Param("total") BigDecimal total);

}
