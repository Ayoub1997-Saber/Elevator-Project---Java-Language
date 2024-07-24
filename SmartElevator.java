/*********************************************************
*Nom binôme 1 : Taihi          
*Prénom binôme 1 : Ayoub       
*                              
*                              
*Nom binôme 2 : Chikhi          
*Prénom binôme 2 : Rayane                                                               
*
*Spécialité : 2ème année Ingénieurs Télécom & Réseaux                              
*Projet : Elevator (étape 3)   
*
**********************************************************/
package org.paumard.elevator.student;

import org.paumard.elevator.Building;
import org.paumard.elevator.Elevator;
import org.paumard.elevator.event.DIRECTION;
import org.paumard.elevator.model.Person;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SmartElevator implements Elevator {

public List<List<Person>> getPeopleByFloor() {
		return peopleByFloor;
	}



public int getCurrentFloor() {
		return currentFloor;
	}


public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}


public void setPeopleByFloor(List<List<Person>> peopleByFloor) {
		this.peopleByFloor = peopleByFloor;
	}


	/**
	 * @return the people
	 */
public List<Person> getPeople() {
		return people;
	}


	/**
	 * @param people the people to set
	 */
public void setPeople(List<Person> people) {
		this.people = people;
	}


	/**
	 * @return the destinations
	 */
public List<Integer> getDestinations() {
		return destinations;
	}


	/**
	 * @param destinations the destinations to set
	 */
	public void setDestinations(List<Integer> destinations) {
		this.destinations = destinations;
	}

	private DIRECTION direction = DIRECTION.UP;
    private static final int ANGER_LIMIT_THRESHOLD = 120;
    private int currentFloor = 1;
	private List<List<Person>> peopleByFloor = List.of();         
    private final String id;
	private List<Integer> destinations = List.of();
	private List<Person> people = new ArrayList<>();
	private List <Integer> nextDestinations = List.of(1);
	private int capacity;

private List <Integer> allFloorsDestinations = List.of(1,2,3,4,5,6,7,8,9,10);
	private String name;
	private LocalTime time;
	private int nextDestination;
	private Object otherElevator;


public SmartElevator(int capacity, String id) {
    	this.name = name;
    	this.capacity = capacity; 
    	this.id = id;
    }
    

public int getNextDestination() {
		return nextDestination;
	}


    public void setNextDestination(int nextDestination) {
		this.nextDestination = nextDestination;
	}
    
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void startsAtFloor(LocalTime time, int initialFloor) {
        this.currentFloor = initialFloor;
    }

    @Override
    public void peopleWaiting(List<List<Person>> peopleByFloor) {
    	this.peopleByFloor = peopleByFloor;
    }

	/**
	 * @return the nextDestination
	 */
	public List<Integer> getNextDestinations() {
		return nextDestinations;
	}
	/**
	 * @param nextDestination the nextDestination to set
	 */
	
public void setNextDestinations(List<Integer> nextDestination) {
		this.nextDestinations = new ArrayList<>(nextDestination);
	}
private List<Integer> findFullFloor() {
			for (int indexFloor = 0 ; indexFloor < Building.MAX_FLOOR ; indexFloor++) {
				if (!peopleByFloor.get(indexFloor).isEmpty()) {
					return List.of(indexFloor + 1);
				}
			}
			return List.of(-1);
		}
	@Override
public List<Integer> chooseNextFloors() {
		if (!this.destinations.isEmpty()) {
	    	 int nextFloor = this.destinations.get(0);
	    	 updateDirection(nextFloor);
	    	 this.nextDestination=this.destinations.get(0);
	    	 
	    // Après avoir terminé la charge des personnes et retirer les étages, demande à l'autre ascenseur de charger //  
	    	 List<Integer> otherDestinations = new ArrayList<>(allFloorsDestinations);
	    	 otherDestinations.removeAll(this.destinations);
	    	 setNextDestinations(destinations);
	    		return this.destinations;
	    	}
	/*	
	  int numberOfPeopleWaiting = countWaitingPeople();  
	for (int indexFloor = 0; indexFloor < Building.MAX_FLOOR; indexFloor++)	
	    if (numberOfPeopleWaiting == 0 && peopleByFloor.get(indexFloor).get(0) == List.of())
	    	return List.of(1);
	  */  
	    
		/*  
		if (direction == DIRECTION.UP) {
            if (currentFloor < Building.MAX_FLOOR) {
                currentFloor++;
            } else {
                this.direction = DIRECTION.DOWN;
                currentFloor--;
            }
        } else {
            if (currentFloor > 1) {
                currentFloor--;
            } else {
                this.direction = DIRECTION.UP;
                currentFloor++;
            }
        }
        
*/

     int indexOfFloor = this.currentFloor - 1;
       	if (!peopleByFloor.get(indexOfFloor).isEmpty() ) {
    		List<Integer> destinationsOfPeopleFromCurrentFloor = DestinationOfAllFloors(peopleByFloor.get(indexOfFloor));
    		for (Integer floor : destinationsOfPeopleFromCurrentFloor) {
				if (floor > this.currentFloor) 
					this.destinations.add(floor);
				
				this.destinations = 
						this.destinations.stream()
						.distinct()
						.sorted()
						.collect(Collectors.toList());
			}
    	}
    	if (!this.destinations.isEmpty()) {
    		if(people.size() < this.capacity) 
    			this.destinations = PeopleAscendingFromTheWaitingList();
    		return this.destinations;
    	}
    	int numberOfPeopleWaitingAtFloors = countWaitingPeople();
    	if (numberOfPeopleWaitingAtFloors > 0) {
			List<Integer> destinationsToPickAngryPeople = destinationsToPickUpAngryPeople(); 
			
			if (!destinationsToPickAngryPeople.isEmpty()) {
				this.destinations = destinationsToPickAngryPeople;
				return this.destinations;
			}
			
			List<Integer> fullFloors = findFullFloor();
			int fullFloor = fullFloors.get(0);
			if (fullFloor != this.currentFloor) {
				return List.of(fullFloor);
			} else {
				int indexOfCurrentFloor = indexOfFloor;
				List<Person> waitingListFromCurrentFloor = 
						this.peopleByFloor.get(indexOfCurrentFloor);
				List<Integer> destinationFloorsForCurrentFloor = 
						DestinationOfAllFloors(waitingListFromCurrentFloor);
				this.destinations = destinationFloorsForCurrentFloor;
			
				return this.destinations;
			}
    	}
    	else 
    	   {
    		if (this.time.isAfter(LocalTime.of(10,30,0)) || 
    				this.time.isAfter( LocalTime.of(12,0,0)) && this.time.isBefore(LocalTime.of(13,0,0))) {
    			return List.of(1);
    		}
    		else 
    			return List.of(10);
    		
    	}
    }

private List<Integer> destinationsToPickUpAngryPeople() {
		
		for (int indexFloor = 0 ; indexFloor < Building.MAX_FLOOR ; indexFloor++) {
			List<Person> waitingList = this.peopleByFloor.get(indexFloor);
			if (!waitingList.isEmpty()) {
				Person mostPatientPerson = waitingList.get(0);
				LocalTime arrivalTime = mostPatientPerson.getArrivalTime();
				Duration waitingTime = Duration.between(arrivalTime, this.time); 
				long waitingTimeInSeconds = waitingTime.toSeconds();
				if (waitingTimeInSeconds >= ANGER_LIMIT_THRESHOLD) {
					List<Integer> result = List.of(indexFloor + 1, mostPatientPerson.getDestinationFloor());
					return new ArrayList<>(result);
				}
			}
		}
		return List.of();
	}

private List<Integer> findNonEmptyFloor() {
	for (int indexFloor = 0 ; indexFloor < Building.MAX_FLOOR ; indexFloor++) {
		if (!peopleByFloor.get(indexFloor).isEmpty()) {
			return List.of(indexFloor + 1);
		}
	}
	return List.of(-1);
}

private List<Integer> sameDestinationForMostPeopleWaiting(){
		
		for (int indexFloor = 0 ; indexFloor < Building.MAX_FLOOR ; indexFloor++) {
			List<Person> waitingList = this.peopleByFloor.get(indexFloor);
			if (!waitingList.isEmpty()) {
				List<Integer> nonEmptyFloors = findNonEmptyFloor();
				Entry<Integer, Long> maxSameDestinationForPersonsWaiting = nonEmptyFloors.stream()
						.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
						.entrySet()
						.stream()
						.max(
						(floorX, floorY) ->
						floorX.getValue() == floorY.getValue()
						?
						Long.compare(floorX.getKey(), floorY.getKey())
						:
						Long.compare(floorX.getValue(), floorY.getValue())
						)
						.get();
	
				
				
				
	    			List<Integer> sameDestinations = List.of(indexFloor + 1, ((Person) maxSameDestinationForPersonsWaiting).getDestinationFloor());
    				return new ArrayList<>(sameDestinations);			
    				
			     }
			}
		
		return List.of();
}
private List<Integer> PeopleAscendingFromTheWaitingList() {
		List<Integer> fullFloors = findFullFloor();
		int fullFloor = fullFloors.get(0);
           
                       
                        
		
		if (fullFloor > this.currentFloor) {
			List<Integer> destinations = DestinationOfAllFloors(peopleByFloor.get(fullFloor - 1));
			List<Integer> newDestinations = new ArrayList<>();
			for (Integer destination : destinations ) {
				if (destination > this.currentFloor) {
					newDestinations.add(destination);
				}
			}
			if (fullFloor > this.currentFloor && !this.destinations.contains(fullFloor)) {
				newDestinations.add(fullFloor);
			}
			newDestinations.addAll(this.destinations);
			newDestinations = newDestinations.stream().distinct().sorted().collect(Collectors.toList());
			return new ArrayList<>(newDestinations);
		}
		return this.destinations;
	}
	
	
	
private void updateDirection(int nextFloor) {
		
		if (nextFloor > this.currentFloor) 
			this.direction = DIRECTION.UP;
		else
			this.direction = DIRECTION.DOWN;   
	}

	
private int countWaitingPeople() {
		return peopleByFloor.stream()
			.mapToInt(list -> list.size())
			.sum();
	}


    @Override
public void arriveAtFloor(int floor) {
    	if (!this.destinations.isEmpty()) {
    		this.destinations.remove(0);
    	}
    	this.currentFloor = floor;
    }

private List<Integer> DestinationOfAllFloors(List<Person> waitingListFromCurrentFloor) {
		return waitingListFromCurrentFloor.stream()
			.map(person -> person.getDestinationFloor())
			.distinct()
			.sorted()
			.collect(Collectors.toList());
	}

    
    @Override
public void loadPeople(List<Person> person) {
    	this.people.addAll(people);
    	waitingListForCurrentFloor().removeAll(people);
    }

private List <Person> waitingListForCurrentFloor(){
    	int indexOfCurrentFloor = this.currentFloor - 1;
    	return this.peopleByFloor.get(indexOfCurrentFloor);
    }

    @Override
public void unload(List<Person> person) {
    	this.people.removeAll(people);
    }

    @Override
public void newPersonWaitingAtFloor(int floor, Person person) {
    	int indexFloor = floor - 1;
    	this.peopleByFloor.get(indexFloor).add(person);
    }

    @Override
public void lastPersonArrived() {
    }

    @Override
public void timeIs(LocalTime time) {
    	this.time = time;
    }

    @Override
public void standByAtFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }
}


