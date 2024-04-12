package brokcu.ca.bf22wk.a2;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Event
{
    public static ArrayList<Event> eventsList = new ArrayList<>();

    // Grabs all events for that date
    public static ArrayList<Event> eventsForDate(Date date)
    {
        ArrayList<Event> events = new ArrayList<>();

        for(Event event : eventsList)
        {
            if(Objects.equals(event.getDate(), date))
                events.add(event);
        }

        return events;
    }

    // Events for that weeek
    public static ArrayList<Event> eventsForWeek(Date date)
    {
        Calendar c = Calendar.getInstance();

        c.setTime(date);
        c.add(Calendar.DAY_OF_YEAR, 1);
        Date startDate = c.getTime();

        c.setTime(date);
        c.add(Calendar.WEEK_OF_YEAR, 1);
        Date endDate = c.getTime();

        ArrayList<Event> events = new ArrayList<>();

        for(Event event : eventsList)
        {
            if(event.getDate().before(endDate) && (Objects.equals(event.getDate(), startDate) ||
                    event.getDate().after(startDate)))
                events.add(event);
        }

        return events;
    }


    private String name;
    private Date date;
    private Long dateLong;
    int ID;
    private final int[] time = new int[2];

    private final String meetingContact;

    public Event(String name, Date date, int hour, int minute, String meetingContact)
    {
        this.name = name;
        this.date = date;
        this.time[0] = hour;
        this.time[1] = minute;
        this.dateLong = date.getTime();
        this.meetingContact = meetingContact;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    public Date getDate()
    {
        return date;
    }

    public Long getDateLong()
    {
        return dateLong;
    }

    // Postpones meetings
    public void postPone(){
        String temp = date.toString().substring(0,3);

        Calendar c = Calendar.getInstance();

        c.setTime(date);

        switch (temp){
            // if saturday, postpone to next saturday
            case "Sat":
                c.add(Calendar.DAY_OF_YEAR, 7);
                break;
            // if sunday, postpone to next saturday
            case "Sun":
                c.add(Calendar.DAY_OF_YEAR, 6);
                break;

            // if saturday, postpone to monday
            case "Fri":
                c.add(Calendar.DAY_OF_YEAR, 2);

            // postpone one day
            default:
                c.add(Calendar.DAY_OF_YEAR, 1);
        }

        this.date = c.getTime();

    }

    public int[] getTime()
    {
        return time;
    }

    public void setID(int id){
        this.ID = id;
    }

    public String getMeetingContact() {
        return meetingContact;
    }

    public int getID() {
        return ID;
    }
}
