package lv.tsi.javacourses.bookshelf.books.boundary;

import lv.tsi.javacourses.bookshelf.auth.boundary.CurrentUser;
import lv.tsi.javacourses.bookshelf.books.model.BookEntity;
import lv.tsi.javacourses.bookshelf.books.model.ReservationEntity;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Named
@ViewScoped
public class MyBooksBean implements Serializable {
    @PersistenceContext
    private EntityManager em;
    @Inject
    private CurrentUser currentUser;
    private List<ReservationEntity> availableResult;
    private List<ReservationEntity> inQueueResult;
    private List<ReservationEntity> takenResult;
    private List<BookEntity> hystoryResult;


    public void prepare() {
        availableResult = new ArrayList<>();
        inQueueResult = new ArrayList<>();
        List<ReservationEntity> userReservations = em.createQuery(
                "select r from Reservation r " +
                        "where r.user = :user and r.status = 'ACTIVE'", ReservationEntity.class)
                .setParameter("user", currentUser.getUser())
                .getResultList();

        for (ReservationEntity r : userReservations) {
            Long reservationId = r.getId();
            Optional<ReservationEntity> firstReservation = em.createQuery(
                    "select r from Reservation r " +
                            "where r.book = :book and r.status <> 'CLOSED' " +
                            "order by r.created", ReservationEntity.class)
                    .setParameter("book", r.getBook())
                    .getResultStream()
                    .findFirst();
            if (firstReservation.isEmpty() || firstReservation.get().getId().equals(reservationId)) {
                availableResult.add(r);
            } else {
                inQueueResult.add(r);
            }
        }

        takenResult = em.createQuery("select r from Reservation r " +
                "where r.user = :user and r.status = 'TAKEN'", ReservationEntity.class)
                .setParameter("user", currentUser.getUser())
                .getResultList();

        hystoryResult = em.createQuery("select distinct r.book from Reservation r " +
                "where r.user = :user and r.status = 'CLOSED'" ,BookEntity.class)
                .setParameter("user", currentUser.getUser())
                .getResultList();
    }

    public List<ReservationEntity> getAvailableBooks() {
        return availableResult;
    }

    public List<ReservationEntity> getInQueueBooks() {
        return inQueueResult;
    }

    public List<ReservationEntity> getTakenBooks() {
        return takenResult;
    }


    public List<BookEntity> getHystoryBooks() {
        return hystoryResult;
    }
}
