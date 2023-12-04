/**
 * Project Calendar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * [Your Project Name] is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project Calendar.  If not, see https://github.com/KenWasHere31/Projects/blob/main/LICENSE.
 */


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormatSymbols;

class Event {
    private String eventName;
    private int day;

    public Event(String eventName, int day) {
        this.eventName = eventName;
        this.day = day;
    }

    public String getEventName() {
        return eventName;
    }

    public int getDay() {
        return day;
    }

    public String toFileString() {
        return eventName + "," + day;
    }
}

class Person {
    private String name;
    private int age;
    private String address;
    private String cys;
    private ArrayList<Event> events;
    private int selectedMonth;

    public Person(String name, int age, String address, String cys) {
        this.name = name;
        this.age = age;
        this.address = address;
        this.cys = cys;
        this.events = new ArrayList<>();
    }

    public void addEvent(String eventName, int day) {
        events.add(new Event(eventName, day));
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public String toFileString() {
        StringBuilder result = new StringBuilder(name + "," + age + "," + address + "," + cys);
        for (Event event : events) {
            result.append(",").append(event.toFileString());
        }
        return result.toString();
    }

    public void setSelectedMonth(int selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    public int getSelectedMonth() {
        return selectedMonth;
    }
}

public class CalendarApp extends JFrame {
    private JLabel monthLabel;
    private JPanel calendarPanel;
    private ArrayList<Person> peopleList;

    private int currentMonth;
    private int currentYear;

    private static final String DATA_FILE = "calendar_data.txt";

    public CalendarApp(ArrayList<Person> peopleList) {
        this.peopleList = peopleList;

        setTitle("Project Calendar");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        // Load data from the file
        loadDataFromFile();

        // Create components
        monthLabel = new JLabel();
        calendarPanel = new JPanel(new GridLayout(7, 7));

        JButton prevButton = new JButton("<< Prev");
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousMonth();
            }
        });

        JButton nextButton = new JButton("Next >>");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextMonth();
            }
        });

        JButton addEventButton = new JButton("Add Event");
        addEventButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddEventDialog();
            }
        });

        JButton addPersonButton = new JButton("Add Person");
        addPersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddPersonDialog();
            }
        });

        // Add components to the frame
        JPanel controlPanel = new JPanel();
        controlPanel.add(prevButton);
        controlPanel.add(monthLabel);
        controlPanel.add(nextButton);
        controlPanel.add(addEventButton);
        controlPanel.add(addPersonButton);

        add(controlPanel, BorderLayout.NORTH);
        add(calendarPanel, BorderLayout.CENTER);

        updateCalendar();

        // Display the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadDataFromFile() {
        try {
            File file = new File(DATA_FILE);
    
            // Check if the file exists; if not, create a new file
            if (!file.exists()) {
                file.createNewFile();
            }
    
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        String name = parts[0];
                        int age = Integer.parseInt(parts[1]);
                        String address = parts[2];
                        String cys = parts[3];
    
                        Person person = new Person(name, age, address, cys);
    
                        for (int i = 4; i < parts.length; i += 2) {
                            String eventName = parts[i];
                            int day = Integer.parseInt(parts[i + 1]);
                            person.addEvent(eventName, day);
                        }
    
                        peopleList.add(person);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
    

    private void saveDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Person person : peopleList) {
                writer.write(person.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAddPersonDialog() {
        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField cysField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);
        panel.add(new JLabel("Course, Year, Section:"));
        panel.add(cysField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Person", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());
            String address = addressField.getText();
            String cys = cysField.getText();

            // Create a new person and add it to the peopleList
            Person newPerson = new Person(name, age, address, cys);
            peopleList.add(newPerson);

            // Save data to the file
            saveDataToFile();

            // Update the calendar
            updateCalendar();
        }
    }

    private void showAddEventDialog() {
        JTextField eventNameField = new JTextField();
        JComboBox<Integer> dayComboBox = new JComboBox<>();
        for (int i = 1; i <= 31; i++) {
            dayComboBox.addItem(i);
        }

        JComboBox<String> monthComboBox = new JComboBox<>(new DateFormatSymbols().getMonths());
        monthComboBox.setSelectedIndex(currentMonth);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Event Name:"));
        panel.add(eventNameField);
        panel.add(new JLabel("Day:"));
        panel.add(dayComboBox);
        panel.add(new JLabel("Month:"));
        panel.add(monthComboBox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Event", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String eventName = eventNameField.getText();
            int day = (int) dayComboBox.getSelectedItem();
            int selectedMonth = monthComboBox.getSelectedIndex();

            // Add the event to the selected month
            addEventToSelectedMonth(eventName, day, selectedMonth);

            // Save data to the file
            saveDataToFile();

            // Update the calendar
            updateCalendar();
        }
    }

    private void addEventToSelectedMonth(String eventName, int day, int selectedMonth) {
        // Check if there is at least one person in the list
        if (!peopleList.isEmpty()) {
            // Find the person object for the current user (replace with actual logic to find the user)
            Person currentUser = peopleList.get(0);

            // Add the event to the selected month for the current user
            currentUser.addEvent(eventName, day);
            currentUser.setSelectedMonth(selectedMonth); // Save the selected month
        } else {
            // Handle the case where the peopleList is empty (e.g., show a message or add a new person)
            JOptionPane.showMessageDialog(this, "No person found. Add a person first.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void updateCalendar() {
        // Update the month label
        String monthName = new DateFormatSymbols().getMonths()[currentMonth];
        monthLabel.setText(monthName + " " + currentYear);

        // Clear the calendarPanel
        calendarPanel.removeAll();

        // Add day labels (Sun, Mon, Tue, etc.)
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : daysOfWeek) {
            calendarPanel.add(new JLabel(day, SwingConstants.CENTER));
        }

        // Get the first day of the month
        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // Sunday is 0

        // Add empty labels for the days before the first day of the month
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }

        // Add labels for each day of the month
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= lastDay; i++) {
            JLabel dayLabel = new JLabel(Integer.toString(i), SwingConstants.CENTER);
            calendarPanel.add(dayLabel);

            // Check if there are events on this day
            for (Person person : peopleList) {
                for (Event event : person.getEvents()) {
                    // Only display events for the selected month
                    if (event.getDay() == i && person.getSelectedMonth() == currentMonth) {
                        dayLabel.setText("<html>" + i + "<br>" + event.getEventName() + "</html>");
                        dayLabel.setForeground(Color.BLUE);
                    }
                }
            }

            // Add mouse listener to show events on click
            final int day = i;
            dayLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showEventsForDay(day);
                }
            });
        }

        // Repaint the calendarPanel
        calendarPanel.revalidate();
        calendarPanel.repaint();

        saveDataToFile();
    }

    private void showEventsForDay(int day) {
        StringBuilder eventInfo = new StringBuilder("Events for Day " + day + ":\n");

        if (peopleList != null) {
            for (Person person : peopleList) {
                if (person.getEvents() != null) {
                    for (Event event : person.getEvents()) {
                        if (event.getDay() == day && person.getSelectedMonth() == currentMonth) {
                            eventInfo.append(person.toString()).append(": ").append(event.getEventName()).append("\n");
                        }
                    }
                }
            }
        }

        JOptionPane.showMessageDialog(this, eventInfo.toString(), "Events for Day " + day, JOptionPane.INFORMATION_MESSAGE);
    }
                  
    private void previousMonth() {
        if (currentMonth == 0) {
            currentMonth = 11;
            currentYear--;
        } else {
            currentMonth--;
        }
        updateCalendar();
    }

    private void nextMonth() {
        if (currentMonth == 11) {
            currentMonth = 0;
            currentYear++;
        } else {
            currentMonth++;
        }
        updateCalendar();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalendarApp(new ArrayList<>()));
    }
}
    
