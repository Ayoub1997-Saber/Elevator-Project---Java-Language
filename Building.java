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
package org.paumard.elevator;

import org.paumard.elevator.event.DIRECTION;
import org.paumard.elevator.event.Event;
import org.paumard.elevator.event.Event.AttemptToLoadFirstPerson;
import org.paumard.elevator.model.Person;
import org.paumard.elevator.model.WaitingList;
import org.paumard.elevator.student.SmartElevator;
import org.paumard.elevator.student.ElevatorOfMyFlatWorksLikeHim;
import org.paumard.elevator.system.Elevators;
import org.paumard.elevator.system.ShadowElevator;
import org.paumard.elevator.system.ShadowElevators;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Building {

    public static PrintStream PRINTER;
    public static final int ELEVATOR_CAPACITY = 15;
    public static final int ELEVATOR_LOADING_CAPACITY = 3;
    public static final int MAX_DISPLAYED_FLOORS = 10;
    public static final int MAX_FLOOR = 10;
    public static final LocalTime START_TIME = LocalTime.of(6, 0, 0);
    public static final LocalTime END_TIME = LocalTime.of(22, 30, 0);
    public static final LocalTime END_OF_DAY = END_TIME.plusHours(1);
    public static Random random = new Random(10L); // 10L
    public static LocalTime time = START_TIME;

    public static void main(String[] args) throws FileNotFoundException {

        System.out.println("Start time = " + START_TIME);
        System.out.println("End time = " + END_TIME);
        System.out.println("End of day = " + END_OF_DAY);

         PRINTER = System.out;
        //PRINTER = new PrintStream("test/debug.log");

        Set<PrintStream> printers = new HashSet<>(List.of(PRINTER, System.out));

        NavigableMap<LocalTime, List<Event>> events = new TreeMap<>();

        WaitingList waitingList = WaitingList.getInstance();
        int totalNumberOfPeople = waitingList.countPeople();
 
        SmartElevator elevator1 = new SmartElevator(ELEVATOR_CAPACITY, "Smart 1");
       // SmartElevator elevator2 = new SmartElevator(ELEVATOR_CAPACITY, "Smart 2");
       // Elevator elevator3 = new DumbElevator(ELEVATOR_CAPACITY, "Dumb 3");
        Elevator ElevatorOfMyFlatWorksLikeHim = new ElevatorOfMyFlatWorksLikeHim(ELEVATOR_CAPACITY, "Elevator of my flat", elevator1);
        Elevators elevators = new Elevators(List.of(ElevatorOfMyFlatWorksLikeHim, elevator1));

        List<Event> startEvents = Event.createStartEventFor(elevators);
        events.put(time, startEvents);

        elevators.peopleWaiting(waitingList);

        Map<String, ShadowElevator> shadowElevatorsRegistry = elevators.getElevators().stream()
                .collect(Collectors.toMap(
                        Elevator::getId,
                        elevator -> new ShadowElevator(ELEVATOR_CAPACITY, elevator.getId(), waitingList)
                ));

        ShadowElevators shadowElevators = new ShadowElevators(shadowElevatorsRegistry);

        waitingList.print();

        while (shadowElevators.areStillRunning() && time.isBefore(END_OF_DAY)) {

            elevators.timeIs(time);

            if (time.equals(END_TIME)) {
                PRINTER.printf("\n[%s]No more people are coming.\n", time.toString());
                shadowElevators.lastPersonArrived();
                elevators.lastPersonArrived();
            }

            if (!events.containsKey(time)) {
                if (time.isBefore(END_TIME)) {
                    totalNumberOfPeople += addNewPersonToWaitingLists(time, waitingList, elevators);
                }
                time = time.plusSeconds(3);
                continue;
            }

            List<Event> nextEvents = events.get(time);
            events.remove(time);

            List<Event> loadingEvents = new ArrayList<>();

            for (Event nextEvent : nextEvents) {

                Elevator elevator = nextEvent.getElevator();
                ShadowElevator shadowElevator = shadowElevators.getShadowElevatorFor(elevator);

                if (nextEvent.getName().equals(Event.STOPPING_AT_FLOOR)) {
                    shadowElevator.stopping();
                }

                Event event = null;
                LocalTime arrivalTime = null;

                if (nextEvent.getName().equals(Event.ELEVATOR_STARTS)) {

                    // charge
                    event = Event.fromElevatorStart(time, elevator, shadowElevator);

                } else if (nextEvent.getName().equals(Event.DOOR_OPENING)) {

                    // charge
                    event = Event.fromDoorOpening(time, elevator, shadowElevator);

                } else if (nextEvent.getName().equals(Event.LOADING_FIRST_PERSON)) {

                    // charge
                    event = Event.fromLoadingFirstPerson(time, shadowElevator, elevator, nextEvent);

                } else if (nextEvent.getName().equals(Event.LOADING_NEXT_PERSON)) {

                    // charge
                    event = Event.fromLoadingNextPerson(time, shadowElevator, elevator, nextEvent);

                } else if (nextEvent.getName().equals(Event.ARRIVES_AT_FLOOR)) {

                    event = Event.fromArrivesAtFloor(time, elevator, shadowElevator);

                } else if (nextEvent.getName().equals(Event.UNLOADING_FIRST_PERSON)) {

                    // charge
                    event = Event.fromUnloadingFirstPerson(time, shadowElevator, elevator, nextEvent);

                } else if (nextEvent.getName().equals(Event.UNLOADING_NEXT_PERSON)) {

                    // charge
                    event = Event.fromUnloadingNextPerson(time, shadowElevator, elevator, nextEvent);

                } else if (nextEvent.getName().equals(Event.DOOR_CLOSING)) {

                    event = Event.fromDoorClosing(time, elevator, shadowElevator);

                } else if (nextEvent.getName().equals(Event.STAND_BY_AT_FLOOR)) {

                    event = Event.fromStandByAtFloor(time, elevator, shadowElevator);

                } else if (nextEvent.getName().equals(Event.STOPPING_AT_FLOOR)) {

                    shadowElevator.stopping();
                }

                if (event != null) {
                    if (!event.getName().equals(Event.FIRST_LOADING_ATTEMPT) &&
                            !event.getName().equals(Event.NEXT_LOADING_ATTEMPT)) {

                        arrivalTime = event.getTimeOfArrivalFrom(time);
                        events.computeIfAbsent(arrivalTime, key -> new ArrayList<>()).add(event);

                    } else {

                        loadingEvents.add(event);
                    }
                }

                if (time.isBefore(END_TIME)) {
                    totalNumberOfPeople += addNewPersonToWaitingLists(time, waitingList, elevators);
                }
            }

            if (!loadingEvents.isEmpty()) {

                Map<Integer, List<Event>> loadingEventsByCurrentFloor =
                        loadingEvents.stream()
                                .collect(Collectors.groupingBy(Event::getCurrentFloor));

                for (Map.Entry<Integer, List<Event>> concurrentEventsByFloor : loadingEventsByCurrentFloor.entrySet()) {

                    int currentFloor = concurrentEventsByFloor.getKey();
                    List<Event> concurrentEvents = concurrentEventsByFloor.getValue();
                    if (concurrentEvents.size() == 1) {

                        Event nextEvent = concurrentEvents.get(0);
                        Elevator elevator = nextEvent.getElevator();
                        ShadowElevator shadowElevator = shadowElevators.getShadowElevatorFor(elevator);

                        Event event = null;
                        LocalTime arrivalTime = null;

                        List<Integer> nextFloors = nextEvent.getNextFloors();
                        int nextFloor = nextFloors.get(0);
                        DIRECTION direction = Event.computeDirection(currentFloor, nextFloors);

                        if (nextEvent.getName().equals(Event.FIRST_LOADING_ATTEMPT)) {

                            List<Person> nextPeopleToLoad = shadowElevator.getNextPeopleToLoad(nextFloors, currentFloor);
                            if (!nextPeopleToLoad.isEmpty()) {
                                event = new Event.LoadingFirstPerson(elevator, nextPeopleToLoad);
                            } else {
                                event = new Event.DoorClosing(elevator, currentFloor, nextFloor);
                            }

                        } else if (nextEvent.getName().equals(Event.NEXT_LOADING_ATTEMPT)) {

                            List<Person> nextPeopleToLoad = shadowElevator.getNextPeopleToLoad(nextFloors, currentFloor);
                            if (!nextPeopleToLoad.isEmpty()) {
                                event = new Event.LoadingNextPerson(elevator, nextPeopleToLoad);
                            } else {
                                event = new Event.DoorClosing(elevator, currentFloor, nextFloor);
                            }
                        }

                        arrivalTime = event.getTimeOfArrivalFrom(time);
                        events.computeIfAbsent(arrivalTime, key -> new ArrayList<>()).add(event);

                    } else {

                        Map<Integer, List<Event>> eventByDestinationFloor = concurrentEvents.stream()
                                .flatMap(event -> event.getNextFloors().stream().map(floor -> Map.entry(floor, event)))
                                .collect(
                                        Collectors.groupingBy(
                                                Map.Entry::getKey,
                                                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                                        ));

                        List<Person> peopleWaitingAtFloor = waitingList.getListFor(currentFloor);
                        for (Person person : peopleWaitingAtFloor) {
                            int destinationFloor = person.getDestinationFloor();
                            List<Event> possibleEvents = eventByDestinationFloor.get(destinationFloor);
                            if (possibleEvents == null) {
                                continue;
                            }
                            // 1st criteria: there is room in the elevator
                            Predicate<Event> roomAvailable =
                                    event -> {
                                        AttemptToLoadFirstPerson attemptToLoadPerson = (AttemptToLoadFirstPerson) event;
                                        int peopleAlreadyLoading = attemptToLoadPerson.getPeopleToLoad().size();
                                        int peopleInElevator = shadowElevatorsRegistry.get(event.getElevator().getId()).getNumberOfPeople();
                                        return (peopleInElevator + peopleAlreadyLoading) < ELEVATOR_CAPACITY;
                                    };
                            possibleEvents = possibleEvents.stream()
                                    .filter(roomAvailable)
                                    .collect(Collectors.toList());
                            // 2nd criteria: fastest travel
                            Function<Event, Integer> timeToReachFloor =
                                    event -> event.getNextFloors().indexOf(destinationFloor);
                            possibleEvents =
                                    possibleEvents.stream().collect(Collectors.groupingBy(timeToReachFloor))
                                            .entrySet().stream()
                                            .min(Map.Entry.comparingByKey())
                                            .map(Map.Entry::getValue)
                                            .orElseGet(ArrayList::new);
                            if (possibleEvents.isEmpty()) {
                                continue;
                            }
                            // 3rd criteria: least number of people
                            Function<Event, Integer> numberOfPeople =
                                    event -> shadowElevatorsRegistry.get(event.getElevator().getId()).getNumberOfPeople();
                            possibleEvents.stream().collect(Collectors.groupingBy(numberOfPeople))
                                    .entrySet().stream()
                                    .min(Map.Entry.comparingByKey())
                                    .map(Map.Entry::getValue)
                                    .orElseGet(ArrayList::new);
                            if (possibleEvents.isEmpty()) {
                                continue;
                            }
                            // 3rd criteria: random draw
                            Event selectedEvent = waitingList.chooseEventFrom(possibleEvents);
                            if (selectedEvent instanceof Event.AttemptToLoadFirstPerson) {
                                Event.AttemptToLoadFirstPerson loadingAttempt = (Event.AttemptToLoadFirstPerson) selectedEvent;
                                waitingList.removePeopleFromFloor(currentFloor, person);
                                loadingAttempt.addPerson(person);
                            } else if (selectedEvent instanceof Event.AttemptToLoadNextPerson) {
                                Event.AttemptToLoadNextPerson loadingAttempt = (Event.AttemptToLoadNextPerson) selectedEvent;
                                waitingList.removePeopleFromFloor(currentFloor, person);
                                loadingAttempt.addPerson(person);
                            }
                        }

                        for (Event nextEvent : concurrentEvents) {
                            Elevator elevator = nextEvent.getElevator();
                            ShadowElevator shadowElevator = shadowElevators.getShadowElevatorFor(elevator);

                            Event event = null;
                            LocalTime arrivalTime = null;

                            List<Integer> nextFloors = nextEvent.getNextFloors();
                            int nextFloor = nextFloors.get(0);

                            if (nextEvent instanceof Event.AttemptToLoadFirstPerson) {

                                Event.AttemptToLoadFirstPerson loadingAttempt = (Event.AttemptToLoadFirstPerson) nextEvent;

                                List<Person> nextPeopleToLoad = loadingAttempt.getPeopleToLoad();
                                if (!nextPeopleToLoad.isEmpty()) {
                                    event = new Event.LoadingFirstPerson(elevator, nextPeopleToLoad);
                                } else {
                                    event = new Event.DoorClosing(elevator, currentFloor, nextFloor);
                                }

                            } else if (nextEvent instanceof Event.AttemptToLoadNextPerson) {

                                Event.AttemptToLoadNextPerson loadingAttempt = (Event.AttemptToLoadNextPerson) nextEvent;

                                List<Person> nextPeopleToLoad = loadingAttempt.getPeopleToLoad();
                                if (!nextPeopleToLoad.isEmpty()) {
                                    event = new Event.LoadingNextPerson(elevator, nextPeopleToLoad);
                                } else {
                                    event = new Event.DoorClosing(elevator, currentFloor, nextFloor);
                                }
                            }

                            arrivalTime = event.getTimeOfArrivalFrom(time);
                            events.computeIfAbsent(arrivalTime, key -> new ArrayList<>()).add(event);
                        }
                    }
                }
            }

            time = time.plusSeconds(3);
        }

        waitingList.print();
        shadowElevators.printPeople();
        PRINTER.printf("[%s] Times up\n", time);
        if (PRINTER != System.out) {
            System.out.printf("[%s] Times up\n", time);
        }
        // shadowElevators.printCounts();
        shadowElevators.printMaxes();
        printDurationHistogram();

        long numberOfPeople =
                Event.durations.values().stream().mapToLong(l -> l).sum();
        Optional<Duration> maxDurationOpt =
                Event.durations.keySet().stream().max(Comparator.naturalOrder());
        if (maxDurationOpt.isPresent()) {
            Duration maxDuration = maxDurationOpt.orElseThrow();
            long sum =
                    Event.durations.entrySet().stream().mapToLong(entry -> entry.getKey().getSeconds() * entry.getValue()).sum();
            Duration averageDuration = Duration.ofSeconds(sum / numberOfPeople);

            printers.forEach(printer -> {
                printer.println("Number of people taken = " + numberOfPeople);
                printer.printf("Average waiting time = %dmn %ds\n",
                        averageDuration.toMinutesPart(), averageDuration.toSecondsPart());
                printer.printf("Max waiting time = %dh %dmn %ds\n",
                        maxDuration.toHoursPart(), maxDuration.toMinutesPart(), maxDuration.toSecondsPart());
                printer.println("People left in floors = " + waitingList.countPeople());
            });
            elevators.getElevators().forEach(
                    elevator -> {
                        printers.forEach(printer -> {
                            printer.println("People left in elevator [" + elevator.getId() + "] = "
                                    + shadowElevators.getShadowElevatorFor(elevator).numberOfPeopleInElevator());
                        });
                    }
            );
        }
    }

    private static int addNewPersonToWaitingLists(LocalTime time, WaitingList peopleWaitingPerFloor, Elevators elevators) {
        Optional<Map.Entry<Integer, Person>> newPersonWaiting = peopleWaitingPerFloor.addNewPeopleToLists(time);
        if (newPersonWaiting.isPresent()) {
            int floor = newPersonWaiting.orElseThrow().getKey();
            Person person = newPersonWaiting.orElseThrow().getValue();
            elevators.newPersonWaitingAtFloor(floor, person);
            PRINTER.printf("\n[%s] %s calls the elevator from floor %d to go to floor %d\n", time, person.getName(), floor, person.getDestinationFloor());
            PRINTER.printf("Waiting list is now:\n");
            peopleWaitingPerFloor.print();
            return 1;
        } else {
            return 0;
        }
    }

    private static void printDurationHistogram() {
        NavigableMap<Duration, Long> durations = new TreeMap<>();

        durations.put(Duration.ofSeconds(30), 0L);
        durations.put(Duration.ofSeconds(180), 0L);
        durations.put(Duration.ofSeconds(360), 0L);
        durations.put(Duration.ofMinutes(15), 0L);
        durations.put(Duration.ofMinutes(30), 0L);
        durations.put(Duration.ofHours(1), 0L);

        for (Map.Entry<Duration, Long> entry : Event.durations.entrySet()) {
            Duration duration = entry.getKey();
            Duration bucket = durations.descendingKeySet().stream()
                    .filter(keyDuration -> duration.compareTo(keyDuration) > 0)
                    .findFirst().orElseThrow();
            durations.merge(bucket, entry.getValue(), Long::sum);
        }

        durations.forEach(
                (duration, count) ->
                        PRINTER.printf("%2dh %2dmn %2ds -> %d\n", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart(), count)
        );

        if (PRINTER != System.out) {
            durations.forEach(
                    (duration, count) ->
                            System.out.printf("%2dh %2dmn %2ds -> %d\n", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart(), count)
            );
        }
    }
}
