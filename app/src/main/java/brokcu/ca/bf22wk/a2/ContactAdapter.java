package brokcu.ca.bf22wk.a2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder>
{
    private final ArrayList<Contact> contacts;
    Context context;
    private final RecyclerViewInterface recyclerViewInterface;

    public ContactAdapter(Context context, ArrayList<Contact> contacts,
                          RecyclerViewInterface recyclerViewInterface)
    {
        this.contacts = contacts;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ContactAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.contact_cell, parent, false);
        return new ContactAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        final Contact contact = contacts.get(position);
        holder.contactNameTV.setText(contact.getName());
        holder.contactPhoneTV.setText(contact.getPhone());
        holder.contactEmailTV.setText(contact.getEmail());
    }

    @Override
    public int getItemCount()
    {
        return contacts.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView contactNameTV, contactPhoneTV, contactEmailTV;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            contactNameTV = itemView.findViewById(R.id.contactNameTV);
            contactPhoneTV = itemView.findViewById(R.id.contactPhoneTV);
            contactEmailTV = itemView.findViewById(R.id.contactEmailTV);

            itemView.setOnClickListener(v -> {
                if (recyclerViewInterface != null){
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION){
                        recyclerViewInterface.onItemClick(position);
                    }
                }
            });
        }
    }
}
