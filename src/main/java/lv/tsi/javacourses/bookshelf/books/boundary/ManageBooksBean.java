package lv.tsi.javacourses.bookshelf.books.boundary;

import lv.tsi.javacourses.bookshelf.books.model.BookEntity;
import lv.tsi.javacourses.bookshelf.books.model.ReservationEntity;
import lv.tsi.javacourses.bookshelf.books.model.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//TRACE
//DEBUG
//INFO
//WARNing
//ERROR

@Named
@ViewScoped
public class ManageBooksBean implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(ManageBooksBean.class);
    @PersistenceContext
    private EntityManager em;
    private List<ReservationEntity> availableResult;
    private List<ReservationEntity> takenResult;

    public void prepare() {
        logger.debug("Preparing books for manager");
        availableResult = new ArrayList<>();
        List<ReservationEntity> userReservations = em.createQuery(
                "select r from Reservation r " +
                        "where r.status = 'ACTIVE'", ReservationEntity.class)
                .getResultList();
        logger.debug("Selected {} reservation",userReservations.size());
        for (ReservationEntity r : userReservations) {
            Long reservationId = r.getId();
            logger.trace("Checking reservation {}",r);
            Optional<ReservationEntity> firstReservation = em.createQuery(
                    "select r from Reservation r " +
                            "where r.book = :book and r.status <> 'CLOSED' " +
                            "order by r.created", ReservationEntity.class)
                    .setParameter("book", r.getBook())
                    .getResultStream()
                    .findFirst();
            if (firstReservation.isEmpty() || firstReservation.get().getId().equals(reservationId)) {
                availableResult.add(r);
            }
        }

        takenResult = em.createQuery("select r from Reservation r " +
                "where r.status = 'TAKEN'", ReservationEntity.class)
                .getResultList();
    }

    @Transactional
    public void giveBook(ReservationEntity reservation){
        logger.info("Giving the book {}", reservation);
    ReservationEntity r = em.merge(reservation);
       r.setStatus(ReservationStatus.TAKEN);
       prepare();
    }
    @Transactional
    public void takeBackBook(ReservationEntity reservation){
        logger.info("Getting the book {} {} {}",reservation);
        ReservationEntity r = em.merge(reservation);
        r.setStatus(ReservationStatus.CLOSED);
        prepare();
    }

    public List<ReservationEntity> getAvailableBooks() {
        return availableResult;
    }

    public List<ReservationEntity> getTakenBooks() {
        return takenResult;
    }

}