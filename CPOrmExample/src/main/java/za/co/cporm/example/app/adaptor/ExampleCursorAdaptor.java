package za.co.cporm.example.app.adaptor;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import za.co.cporm.example.app.model.domain.User;
import za.co.cporm.model.loader.CPOrmCursorAdaptor;

/**
 * Created by hennie.brink on 2015-05-16.
 */
public class ExampleCursorAdaptor extends CPOrmCursorAdaptor<User, ExampleCursorAdaptor.ViewHolder> {


    public ExampleCursorAdaptor(Context context) {

        super(context, android.R.layout.simple_list_item_1);
    }

    @Override
    public ViewHolder createViewHolder(View view) {

        return new ViewHolder(view);
    }

    @Override
    public void setViewInformation(ViewHolder viewHolder, User information) {

        viewHolder.textView.setText(information.getUserName());
    }

    protected static class ViewHolder {

        final TextView textView;

        ViewHolder(View view) {

            textView = (TextView) view.findViewById(android.R.id.text1);
        }
    }

}
