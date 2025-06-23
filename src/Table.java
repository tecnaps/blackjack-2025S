import java.util.ArrayList;
import java.util.List;

public class Table {

    // This class implements a blackjack table

    private final List <Seat> seats;
    private final int numberOfSeats;

    // put bets here

    public Table(int numberOfSeats){
        this.seats = new ArrayList<>();
        this.numberOfSeats = numberOfSeats;
        for(int i = 0; i < numberOfSeats; i++)
            seats.add(new Seat(i));
    }

    public int getNumberOfSeats(){
        return numberOfSeats;
    }

    public List<Seat> getSeats(){
        return seats;
    }

    public Seat getSeat(int position){
        return seats.get(position);
    }

    public List<Seat> getOccupiedSeats(){
        return seats.stream().filter(Seat::isOccupied).toList();
    }


}
