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
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ElevatorOfMyFlatWorksLikeHim implements Elevator {
    private static final int ANGER_LIMIT_THRESHOLD = 120;
	private DIRECTION direction = DIRECTION.UP;
    private int currentFloor = 1;
	private List<List<Person>> peopleByFloor = List.of();          
	private List<Person> people = new ArrayList<>();
	private final int capacity;
	private LocalTime time;
	private List<Integer> destinations = List.of();
    private int nextDestination;
	private SmartElevator otherElevator;
	private String name;
    private List <Integer> allFloorsDestinations = List.of(1,2,3,4,5,6,7,8,9,10);
	private Random random = new Random (314L);
    
public ElevatorOfMyFlatWorksLikeHim(int capacity, String name, SmartElevator otherElevator) {
    
    	this.name = name;
	this.otherElevator = otherElevator;
    	this.capacity = capacity;
    }


    @Override
    public List<Integer> chooseNextFloors() {
	
    	if (!this.destinations.isEmpty()) {
        	 int nextFloor = this.destinations.get(0);
    	     updateDirection(nextFloor);
    	     this.nextDestination=this.destinations.get(0);
       	      List<Integer> otherDestinations = new ArrayList<>(allFloorsDestinations);
    	      otherDestinations.removeAll(this.destinations);
    	      this.otherElevator.setNextDestinations(destinations);
    	      return this.destinations;
    	}	
    	
    	
    	 List<Person> waitingListForCurrentFloor = waitingListForCurrentFloor();
			 List<Integer> destinationFloorsForCurrentFloor = findDestinationFloors(waitingListForCurrentFloor);
			 
    for (int indexFloor = 0; indexFloor < Building.MAX_FLOOR; indexFloor++) {	
      List <Integer> nextDestinationOfOtherElevator = new ArrayList <>(otherElevator.getNextDestinations());
    	if(people.size() <= this.capacity) {	
    	  if (!this.destinations.isEmpty() && !this.otherElevator.getDestinations().isEmpty()) {
    	    if (nextDestinationOfOtherElevator.get(indexFloor) == this.nextDestination){
    	    	if(peopleByFloor.get(indexFloor).size() < this.otherElevator.getPeopleByFloor().size()) { 
               
    		 
 			 this.destinations = destinationFloorsForCurrentFloor;
   		     int nextFloor = this.destinations.get(indexFloor);
         	 updateDirection(nextFloor);
      		return this.destinations;
    	    	}
      	
      	  else {
      		  if(Math.abs(currentFloor - nextDestination) > Math.abs(this.otherElevator.getCurrentFloor() - this.otherElevator.getNextDestination())) {
       		  this.destinations = destinationFloorsForCurrentFloor;	
      	      	  
    		  int nextFloor = this.otherElevator.getNextDestinations().get(indexFloor);
              updateDirection(nextFloor);
         	return this.otherElevator.getNextDestinations();		
      		  }
      		  else {
      			 this.destinations = destinationFloorsForCurrentFloor;
      		     int nextFloor = this.destinations.get(indexFloor);
            	 updateDirection(nextFloor);
         		 return this.destinations;
       	    	  
            }
           } 
       	  }  
    	    if (nextDestinationOfOtherElevator.get(indexFloor) == this.nextDestination && nextDestinationOfOtherElevator.get(indexFloor) == this.nextDestination && Math.abs(currentFloor - nextDestination) == Math.abs(this.otherElevator.getCurrentFloor() - this.otherElevator.getNextDestination()) && peopleByFloor.get(indexFloor).size() == this.otherElevator.getPeopleByFloor().size()){
    			
    	    	int nextFloor =  this.destinations.get(indexFloor) + random.nextInt(destinations.get(indexFloor) - this.otherElevator.getDestinations().get(indexFloor));
    		     updateDirection(nextFloor);
        		 return this.destinations;
      	       	 
          }  
         }	  
  	    }
       }  
    
   
    	int numberOfPeopleWaiting = countWaitingPeople();       
    	 
    	if (numberOfPeopleWaiting > 0) {
    		
    		int numberOfPeopleWaitingAtCurrentFloor = waitingListForCurrentFloor().size();
    	    	if (numberOfPeopleWaitingAtCurrentFloor > 0) {
    			    this.destinations = destinationFloorsForCurrentFloor;
    	     		int nextFloor = this.destinations.get(0);
         		   	updateDirection(nextFloor);
         		   	
        			this.otherElevator.setNextDestinations(List.of(random.nextInt(10)+1));
    	   		return this.destinations;
    		}
    			
    		List<Integer> destinations = destinationsToPickUpAngryPeople();
    	    	if (!destinations.isEmpty()) {
    	    		this.destinations = destinations;
    	    	 	this.nextDestination = this.destinations.get(0);
         			this.otherElevator.setNextDestinations(List.of(random.nextInt(10)+1));
       			return this.destinations;
    		}
    		
    		if (numberOfPeopleWaitingAtCurrentFloor == 0) {
    			int firstNonEmptyFloor = findFirstNonEmptyFloor();
    			this.nextDestination = firstNonEmptyFloor; 
    			this.otherElevator.setNextDestinations(List.of(random.nextInt(10)+1));
    			return List.of(firstNonEmptyFloor);
    		}
   
    }
/*    	
		if (numberOfPeopleWaiting >= Building.ELEVATOR_CAPACITY) {
    		if (numberOfPeopleWaiting == Building.ELEVATOR_CAPACITY) {
    			List<Integer> destinationForSameFloor = sameDestinationForMostPeopleWaiting();
    			this.destinations = destinationForSameFloor;
    			return this.destinations;
    		}
    		
    	else {
 			   if(this.time.getHour() <= 10 && this.time.getHour() > 7) {
	               List <Integer> destinationWithHighestAffluence = List.of(1);
	           	this.otherElevator.setNextDestinations(List.of(1));
	               this.destinations = destinationWithHighestAffluence;
	               return this.destinations;             
 	   }
 			   	   
 			   if(this.time.getHour() <= 20 && this.time.getHour() > 16) {
             List <Integer> destinationInTheMiddleOfElevator = List.of(5);
         	this.otherElevator.setNextDestinations(List.of(5));
	          this.destinations = destinationInTheMiddleOfElevator;
	              return this.destinations;       	   
 		  }
      } 

    			if (!destinations.isEmpty()) {
        		List<Integer> destination = destinationsToPickUpAngryPeople();
    			this.destinations = destination;
    			return this.destinations;	
    	   } 	
       }	
*/
    	
 int indexOfFloor = this.currentFloor - 1;
    if ( !peopleByFloor.get(indexOfFloor).isEmpty()) {
    		List<Integer> destinationsOfPeopleFromCurrentFloorElevator1 = DestinationOfAllFloors(peopleByFloor.get(indexOfFloor));
    	for (Integer floorElevator1 : destinationsOfPeopleFromCurrentFloorElevator1) {
			if (floorElevator1 > this.currentFloor) 
					this.destinations.add(floorElevator1);
				this.destinations = this.destinations.stream().distinct().sorted().collect(Collectors.toList());
	
    	    }
    	}
   if (!this.otherElevator.getPeopleByFloor().get(indexOfFloor).isEmpty()) {
		List<Integer> destinationsOfPeopleFromCurrentFloorElevator2 = DestinationOfAllFloors(this.otherElevator.getPeopleByFloor().get(indexOfFloor));
    	for (Integer floorElevator2 : destinationsOfPeopleFromCurrentFloorElevator2) {
			if (floorElevator2 > this.currentFloor) 
					this.destinations.add(floorElevator2);
				this.destinations = this.destinations.stream().distinct().sorted().collect(Collectors.toList());
	
    	    }
       
   }

   
  
		this.nextDestination = 1;
	//	this.otherElevator.setNextDestinations(List.of(random.nextInt(10)+1));	
                  return List.of(1);
    } 
    
    @Override
public void startsAtFloor(LocalTime time, int initialFloor) {
		this.time = time;
    }

    @Override
public void peopleWaiting(List<List<Person>> peopleByFloor) {
    	this.peopleByFloor = peopleByFloor;
    }

    
private List<Integer> findFullFloor() {
		for (int indexFloor = 0 ; indexFloor < Building.MAX_FLOOR ; indexFloor++) {
			if (!peopleByFloor.get(indexFloor).isEmpty()) 
				return List.of(indexFloor + 1);
			
		}
		return List.of(-1);
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
			newDestinations = newDestinations.stream()
					.distinct()
					.sorted()
					.collect(Collectors.toList());
			return new ArrayList<>(newDestinations);
		}
		return this.destinations;
	}
	
	

private List<Integer> DestinationOfAllFloors(List<Person> waitingListFromCurrentFloor) {
		return waitingListFromCurrentFloor.stream()
			.map(person -> person.getDestinationFloor())
			.distinct()
			.sorted()
			.collect(Collectors.toList());
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
	
	
	
	
private List<Integer> findDestinationFloors(List<Person> waitingListForCurrentFloor) {
		return waitingListForCurrentFloor.stream()
			.map(person -> person.getDestinationFloor())
			.distinct()
			.sorted()
			.collect(Collectors.toList());
	}

	
private int findFirstNonEmptyFloor() {
	
	    List <Integer> nonEmptyFloors = findNonEmptyFloor();
	    int firstNonEmptyFloor = nonEmptyFloors.get(0);
		return firstNonEmptyFloor;
		}


private List<Integer> findNonEmptyFloor() {
	for (int indexFloor = 0 ; indexFloor < Building.MAX_FLOOR ; indexFloor++) {
		if (!peopleByFloor.get(indexFloor).isEmpty()) {
			return List.of(indexFloor + 1);
		}
	}
	return List.of(-1);
}


private List<Integer> findDestinationOfFloors (List<Person> waitingListInTheCurrentFloor){
	List<Integer> higherFloors =   waitingListInTheCurrentFloor.stream()
			                       .map(person -> person.getDestinationFloor())
			                       .filter(destinationFloor -> destinationFloor > this.currentFloor)
			                       .distinct()
			                       .sorted(Comparator.<Integer>naturalOrder().reversed())
			                       .collect(Collectors.toList());
	
	List<Integer> lowerFloors =   waitingListInTheCurrentFloor.stream()
            .map(person -> person.getDestinationFloor())
            .filter(destinationFloor -> destinationFloor < this.currentFloor)
            .distinct()
            .sorted(Comparator.<Integer>naturalOrder().reversed())
            .collect(Collectors.toList());
	
	if (higherFloors.isEmpty())
		return lowerFloors;
	if(this.direction == DIRECTION.UP) {
		if (lowerFloors.isEmpty()) {
			this.direction = DIRECTION.DOWN;
		}
	}	
	if(this.direction == DIRECTION.UP) {
	    higherFloors.addAll(higherFloors);
	    return higherFloors;	
	}
	
	else 
		return lowerFloors;
}


private int countWaitingPeople() {
		return peopleByFloor.stream()
			.mapToInt(list -> list.size())
			.sum();
	}


private List <Person> waitingListForCurrentFloor(){
	int indexOfCurrentFloor = this.currentFloor - 1;
	return this.peopleByFloor.get(indexOfCurrentFloor);
}


private void updateDirection(int nextFloor) {
	
	if (nextFloor > this.currentFloor) 
		this.direction = DIRECTION.UP;
	else
		this.direction = DIRECTION.DOWN;   
}


    @Override
public void arriveAtFloor(int floor) {
    	if (!this.destinations.isEmpty()) {
    		this.destinations.remove(0);
    	}
    	this.currentFloor = floor;
    }

    @Override
public void loadPeople(List<Person> people) {

    	this.people.addAll(people);
    	waitingListForCurrentFloor().removeAll(people);
    
   }
    @Override
public void unload(List<Person> people) {
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

	@Override
	public String getId() {
		return this.name;
	}
}

