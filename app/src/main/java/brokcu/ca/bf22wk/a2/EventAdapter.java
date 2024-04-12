package brokcu.ca.bf22wk.a2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder>
{
    public final ArrayList<Event> events;
    Context context;
    private final RecyclerViewInterface recyclerViewInterface;

    public EventAdapter(Context context, ArrayList<Event> events,
                        RecyclerViewInterface recyclerViewInterface)
    {
        this.events = events;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public EventAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.event_view, parent, false);
        return new EventAdapter.MyViewHolder(view, recyclerViewInterface, events);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        final Event event = events.get(position);
        int[] time = event.getTime();
        holder.eventNameTV.setText(event.getName());
        holder.eventDateTV.setText(CalendarUtils.formatDate(event.getDate()));
        holder.eventTimeTV.setText(CalendarUtils.formattedTime(time[0], time[1]));
        holder.participantNameTV.setText(String.format("Participant: %s",
                event.getMeetingContact().split(",")[0]));
    }

    @Override
    public int getItemCount()
    {
        return events.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView eventNameTV, eventTimeTV, eventDateTV, participantNameTV;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface,
                            ArrayList<Event> events) {
            super(itemView);
            participantNameTV = itemView.findViewById(R.id.participantNameTV);
            eventNameTV = itemView.findViewById(R.id.eventNameTV);
            eventTimeTV = itemView.findViewById(R.id.eventTimeTV);
            eventDateTV = itemView.findViewById(R.id.eventDateTV);

            itemView.setOnClickListener(v -> {
                if (recyclerViewInterface != null){
                    int position = getAdapterPosition();


                    if (position != RecyclerView.NO_POSITION){
                        // Registers what event the user clicks on
                        CalendarUtils.selectedEvent = events.get(position);
                        recyclerViewInterface.onItemClick(position);
                    }
                }
            });
        }
    }
}
