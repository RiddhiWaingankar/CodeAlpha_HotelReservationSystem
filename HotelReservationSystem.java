import java.io.*;
import java.util.*;

class Room {
    private int roomNumber;
    private String category; // Standard, Deluxe, Suite
    private double pricePerNight;
    private boolean isAvailable;

    public Room(int roomNumber, String category, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
    }

    public int getRoomNumber() { return roomNumber; }
    public String getCategory() { return category; }
    public double getPricePerNight() { return pricePerNight; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public void displayRoom() {
        System.out.printf("Room %d | Category: %s | Price: $%.2f/night | Status: %s%n",
                roomNumber, category, pricePerNight, (isAvailable ? "Available" : "Booked"));
    }
}

class Reservation {
    private String reservationId;
    private String guestName;
    private int roomNumber;
    private int nights;
    private double totalAmount;

    public Reservation(String reservationId, String guestName, int roomNumber, int nights, double totalAmount) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.nights = nights;
        this.totalAmount = totalAmount;
    }

    public String getReservationId() { return reservationId; }
    public int getRoomNumber() { return roomNumber; }
    public String getGuestName() { return guestName; }

    @Override
    public String toString() {
        return String.format("ID: %s | Guest: %s | Room: %d | Nights: %d | Total: $%.2f",
                reservationId, guestName, roomNumber, nights, totalAmount);
    }
}

public class HotelReservationSystem {
    private static List<Room> rooms = new ArrayList<>();
    private static List<Reservation> reservations = new ArrayList<>();
    private static final String FILE_NAME = "bookings.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        initializeRooms();
        loadReservationsFromFile();

        boolean running = true;
        while (running) {
            System.out.println("\n=== Hotel Reservation System ===");
            System.out.println("1. Search / View Available Rooms");
            System.out.println("2. Book a Room");
            System.out.println("3. View Booking Details");
            System.out.println("4. Cancel Reservation");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    displayRooms();
                    break;
                case 2:
                    bookRoom(scanner);
                    break;
                case 3:
                    viewBookings();
                    break;
                case 4:
                    cancelReservation(scanner);
                    break;
                case 5:
                    running = false;
                    saveReservationsToFile();
                    System.out.println("Exiting system. Data saved successfully. Fantastic job completing all your CodeAlpha internship tasks!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void initializeRooms() {
        rooms.add(new Room(101, "Standard", 80.00));
        rooms.add(new Room(102, "Standard", 80.00));
        rooms.add(new Room(201, "Deluxe", 150.00));
        rooms.add(new Room(202, "Deluxe", 150.00));
        rooms.add(new Room(301, "Suite", 300.00));
    }

    private static void displayRooms() {
        System.out.println("\n--- Room Inventory ---");
        for (Room room : rooms) {
            room.displayRoom();
        }
    }

    private static void bookRoom(Scanner scanner) {
        displayRooms();
        System.out.print("Enter room number to book: ");
        int roomNum = scanner.nextInt();

        Room selectedRoom = null;
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNum && r.isAvailable()) {
                selectedRoom = r;
                break;
            }
        }

        if (selectedRoom == null) {
            System.out.println("Error: Room is either invalid or already booked.");
            return;
        }

        scanner.nextLine(); // consume newline
        System.out.print("Enter guest name: ");
        String guestName = scanner.nextLine();

        System.out.print("Enter number of nights: ");
        int nights = scanner.nextInt();

        double totalAmount = selectedRoom.getPricePerNight() * nights;
        System.out.printf("Total Amount to Pay: $%.2f%n", totalAmount);
        System.out.print("Simulate Payment? (yes/no): ");
        String confirm = scanner.next();

        if (confirm.equalsIgnoreCase("yes")) {
            selectedRoom.setAvailable(false);
            String resId = "RES" + (1000 + new Random().nextInt(9000));
            Reservation res = new Reservation(resId, guestName, roomNum, nights, totalAmount);
            reservations.add(res);
            saveReservationsToFile();
            System.out.println("Payment successful! Room booked. Your Reservation ID is: " + resId);
        } else {
            System.out.println("Booking cancelled due to payment failure or cancellation.");
        }
    }

    private static void viewBookings() {
        System.out.println("\n--- Current Reservations ---");
        if (reservations.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        for (Reservation res : reservations) {
            System.out.println(res);
        }
    }

    private static void cancelReservation(Scanner scanner) {
        scanner.nextLine(); // consume newline
        System.out.print("Enter Reservation ID to cancel: ");
        String resId = scanner.nextLine();

        Reservation targetRes = null;
        for (Reservation res : reservations) {
            if (res.getReservationId().equalsIgnoreCase(resId)) {
                targetRes = res;
                break;
            }
        }

        if (targetRes == null) {
            System.out.println("Reservation ID not found.");
            return;
        }

        // Free up room
        for (Room r : rooms) {
            if (r.getRoomNumber() == targetRes.getRoomNumber()) {
                r.setAvailable(true);
                break;
            }
        }

        reservations.remove(targetRes);
        saveReservationsToFile();
        System.out.println("Reservation " + resId + " has been successfully cancelled.");
    }

    private static void saveReservationsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Reservation res : reservations) {
                writer.println(res.getReservationId() + "," + res.getGuestName() + "," + res.getRoomNumber());
            }
        } catch (IOException e) {
            System.out.println("Error saving bookings to file.");
        }
    }

    private static void loadReservationsFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String resId = parts[0];
                    String guest = parts[1];
                    int roomNum = Integer.parseInt(parts[2]);
                    // Mark room as booked in memory
                    for (Room r : rooms) {
                        if (r.getRoomNumber() == roomNum) {
                            r.setAvailable(false);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading bookings from file.");
        }
    }
}