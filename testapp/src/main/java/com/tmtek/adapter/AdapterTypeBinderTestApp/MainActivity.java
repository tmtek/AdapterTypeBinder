package com.tmtek.adapter.AdapterTypeBinderTestApp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.tmtek.adapter.AdapterTypeBinder;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		final RecyclerView listView = findViewById(R.id.list);

		final Adapter listAdapter = new Adapter(
			new MyDataObject[]{
				new MyDataObject("Test Item 1"),
				new MyColoredDataObject("Test Item 2", Color.RED),
				new MyDataObject("Test Item 3", true),
				new MyColoredDataObject("Test Item 4", Color.GREEN),
				new MyDataObject("Test Item 5", false, R.mipmap.ic_launcher)
			}
		);
		listView.setLayoutManager(new LinearLayoutManager(this));
		listView.setAdapter(listAdapter);
	}

	private static class MyDataObject {

		public final String name;
		public final boolean headline;
		public int imageResource = -1;

		public MyDataObject(final String name) {
			this(name, false);
		}

		public MyDataObject(final String name, final boolean headline) {
			this(name, headline, -1);
		}

		public MyDataObject(final String name, final boolean headline, final int imageResource) {
			this.name = name;
			this.headline = headline;
			this.imageResource = imageResource;
		}
	}

	private static class MyColoredDataObject extends MyDataObject{

		public final int color;

		public MyColoredDataObject(final String name, final int color) {
			super(name);
			this.color = color;
		}
	}

	private static class CustomViewHolder extends RecyclerView.ViewHolder {

		public TextView textView;

		public CustomViewHolder(final int layoutResource, final ViewGroup parentView) {
			super(
				LayoutInflater.from(parentView.getContext())
				.inflate(layoutResource, parentView, false)
			);
			textView = itemView.findViewById(R.id.text);
		}
	}

	private static class CustomViewHolderImage extends RecyclerView.ViewHolder {

		public ImageView imageView;

		public CustomViewHolderImage(final int layoutResource, final ViewGroup parentView) {
			super(
				LayoutInflater.from(parentView.getContext())
					.inflate(layoutResource, parentView, false)
			);
			imageView = itemView.findViewById(R.id.image);
		}
	}

	private static final class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private final MyDataObject[] mItems;

		private final AdapterTypeBinder<MyDataObject, RecyclerView.ViewHolder> mTypeBinder =
			new AdapterTypeBinder<MyDataObject, RecyclerView.ViewHolder>()
			.add(
				new AdapterTypeBinder.Binding<>(MyDataObject.class, CustomViewHolderImage.class)
				.isMatch(data -> data.imageResource > -1)
				.onCreate(parentView -> new CustomViewHolderImage(R.layout.list_view_holder_image, parentView))
				.onBind((data, viewHolder) -> {
					viewHolder.imageView.setImageResource(data.imageResource);
				})
			)
			.add(
				new AdapterTypeBinder.Binding<>(MyColoredDataObject.class, CustomViewHolder.class)
				.onCreate(parentView -> new CustomViewHolder(R.layout.list_view_holder, parentView))
				.onBind((data, viewHolder) -> {
					viewHolder.textView.setText(data.name + " set by color Binder");
					viewHolder.textView.setTextColor(data.color);
				})
			)
			.add(
				new AdapterTypeBinder.Binding<>(MyDataObject.class, CustomViewHolder.class)
				.isMatch(data -> data.headline)
				.onCreate(parentView -> new CustomViewHolder(R.layout.list_view_holder_headline, parentView))
				.onBind((data, viewHolder) -> {
					viewHolder.textView.setText(data.name + " set by headline Binder");
				})
			)
			.add(
				new AdapterTypeBinder.Binding<>(MyDataObject.class, CustomViewHolder.class)
				.onCreate(parentView -> new CustomViewHolder(R.layout.list_view_holder, parentView))
				.onBind((data, viewHolder) -> {
					viewHolder.textView.setText(data.name + " set by default Binder");
				})
			);

		public Adapter(final MyDataObject[] items) {
			mItems = items;
		}

		@NonNull
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
			return mTypeBinder.onCreateViewHolder(parent, viewType);
		}

		@Override
		public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
			mTypeBinder.onBindViewHolder(holder, mItems[position]);
		}

		@Override
		public int getItemViewType(int position) {
			return mTypeBinder.getItemViewType(mItems[position]);
		}

		@Override
		public int getItemCount() {
			return mItems.length;
		}
	}
}