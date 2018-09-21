# AdapterTypeBinder

`com.tmtek.adapter.AdapterTypeBinder`

The AdapterTypeBinder is used with Android list Adapters, to map list data to ViewHolder types for rendering. This is useful where you have data in a list that are of different types, or if the data contains contents that would require it to be rendered differently.

### Instantiation:

When you instantiate your AdapterTypeBinder, you will need to specify it's base data type, and it's base view holder type. The following is the most basic definition possible:

```
AdapterTypeBinder<Object, RecyclerView.ViewHolder> typeBinder;
```

### Preparing your Adapter

The AdapterTypeBinder is added to an Adapter subclass, and takes over all of the work regarding ViewHolder creation and binding:

```
private static final class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final MyDataObject[] mItems;

	private final AdapterTypeBinder<Object, RecyclerView.ViewHolder> mTypeBinder;
	
	public Adapter(final MyDataObject[] items) {
		mItems = items;
		mTypeBinder = new AdapterTypeBinder<Object, RecyclerView.ViewHolder>();
	}

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

```

### ViewHolder Bindings

Once our Adapter is prepared, we can now add bindings that will map our data to different ViewHolders. You can call this right off the back of your constructor for the AdapterTypeBinder:

```
mTypeBinder.add(
	new AdapterTypeBinder.Binding<>(MyDataType.class, MyViewHolder.class)
	.isMatch(dataItem -> true)
	.onCreate(parentView -> new MyViewHolder(parentView))
	.onBind((dataItem, viewHolder) -> {
		//bind data to your ViewHolder
	})
).add(...).add(...)

```

The `.add()` method receives an instance of a `AdapterTypeBinder.Binding`. These objects are what you use to define all of your binding rules. The method returns a self reference for convenient chaning.

`new AdapterTypeBinder.Binding<>(MyDataType.class, MyViewHolder.class)` : When instantiating a Binding, you must specify what specific type of data you are mapping, then the specific ViewHolder type you are mapping to.

`.isMatch(dataItem -> true)` is an optional predicate you can supply that is called if the data being checked against this Binder is of the matching type. This predicate can then look at the data and if there are elements of the data's contents you want to discriminate on, you can do so. Returning true from this method means the Binder will operate on it, and not so for a false return.

`.onCreate(parentView -> new MyViewHolder(parentView))` is a function called when a match to your Binder is made, and a new instance of a ViewHolder must be created. The function receives a reference to the parent view that will allow you to build your ViewHolder and return it.

`
.onBind((dataItem, viewHolder) -> {
    //bind data to your ViewHolder
})
` is called when the adapter is trying to bind data to your ViewHolder. Supply a BiConsumer that receives a reference to the data item being bound, and a reference to the ViewHolder being bound to.

### Matching Cascade

It is important to note that it is possible for multiple Binders to match data items. In this case, they will operated on by the first Binder added that matches. You should always add a Binder last that acts as your default that binds your most basic data to your most basic ViewHolder.

