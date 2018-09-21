package com.tmtek.adapter;

import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * AdapterTypeBinder instances are used to match items in a list with appropriate ViewHolder types when rendering
 * any list using an Adapter. These are effective when you have different ViewHolders to use for subclasses of
 * the base data type for the list, or if each item has contents that would require it to be rendered differently.
 * @param <BaseDataType> The class that your Adapter uses as it's base list item type.
 * @param <ViewHolderType> The class that your Adapter uses as it's base ViewHolder type.
 */
public class AdapterTypeBinder<BaseDataType, ViewHolderType> {

	/*
	Included these to not have to lean on higher SDK Android levels.
	 */
	public interface Predicate<T> {
		public abstract boolean test(T testSubject);
	}

	public interface Function<T, R> {
		public abstract R apply(T arg);
	}

	public interface BiConsumer<T, T2> {
		public abstract void accept(T arg, T2 arg2);
	}

	/**
	 * Create instances of Binding to create a connection between list items being managed by your Adapter,
	 * and ViewHolder types to use to render them. Binders allow you to define matching conditions for the data,
	 * it requires that you supply ViewHolder creation capability, and finally, it requires you to supply
	 * ViewHolder binding capability.
	 *
	 * Bindings make a match following this sequence of events:
	 *
	 * 1: Does the Type of the item the Adapter wants to create or bind match the ExtendedDataType of this Binder?
	 *
	 * 2: If it does, then we will call the .isMatch(item) method of the binder to check if
	 *    there are any other match conditions.
	 *
	 * 3: If we have a match, then the onCreate() and onBind() submissions for this Binder
	 *    will operate on the item in question.
	 *
	 * @param <ExtendedDataType> Specify the specific data type that this Binder will be looking to match to.
	 *                          This type can be the BaseDataType of your AdapterTypeBinder,
	 *                          or it can be a subclass of it.
	 * @param <ExtendedViewHolderType> Specify the specific ViewHolder type that this Binder will be supplying to
	 *                                the adapter when a match is made.   .
	 */
	public static class Binding<ExtendedDataType, ExtendedViewHolderType> {
		private final Class<ExtendedDataType>                              itemClass;
		private final Class<ExtendedViewHolderType>                        viewHolderClass;
		private       Predicate<ExtendedDataType>                          bindPredicate;
		private       Function<ViewGroup, ExtendedViewHolderType>          createFunction;
		private       BiConsumer<ExtendedDataType, ExtendedViewHolderType> bindConsumer;
		/**
		 * When constructing a Binder, we require references to the actual classes
		 * that are your ExtendedDataType, and ExtendedViewHolderType respectively.
		 * @param itemClass A reference to the Class object for your ExtendedDataType.
		 * @param viewHolderClass A reference to the Class object for your ExtendedViewHolderType.
		 */
		public Binding(final Class<ExtendedDataType> itemClass,
		               final Class<ExtendedViewHolderType> viewHolderClass
		) {
			this.itemClass = itemClass;
			this.viewHolderClass = viewHolderClass;
		}
		/**
		 * Supply a Predicate(An if statement in object form) that will be checked if the AdapterTypeBinder has matched
		 * your ExtendedDataType for this Binder to the Type of the item data it is trying to work on.
		 *
		 * Using this method is optional. It is only needed in situations when the contents of the
		 * item are a factor in what ViewHolder we want to use.
		 * @param predicate A Predicate<ExtendedDataType>. The argument is an instance of the data we are matching against.
		 *                  A lambda example: .isMatch(item -> item.size > 100)
		 * @return A self reference.
		 */
		public Binding<ExtendedDataType, ExtendedViewHolderType> isMatch(Predicate<ExtendedDataType> predicate) {
			bindPredicate = predicate;
			return this;
		}
		/**
		 * Supply a Function to execute when we have a match and the AdapterTypeBinder wants you to supply a new
		 * instance of your ExtendedViewHolderType for this Binder.
		 * @param function A Function<ViewGroup, ExtendedViewHolderType> where the first argument is the parent ViewGroup
		 *                 supplied by the Adapter.
		 * @return A self reference.
		 */
		public Binding<ExtendedDataType, ExtendedViewHolderType> onCreate(Function<ViewGroup, ExtendedViewHolderType> function) {
			createFunction = function;
			return this;
		}
		/**
		 * Supply a Function to execute when we have a match and the AdapterTypeBinder wants you to bind data from a
		 * list item to a ViewHolder you have already created. Using this method is technically optional, even though
		 * most of the time you will be binding data.
		 *
		 * @param consumer A BiConsumer<ExtendedDataType, ExtendedViewHolderType> where the first argument is the list
		 *                 item data to bind, and the second is the ViewHolder you created previously.
		 * @return A self reference.
		 */
		public Binding<ExtendedDataType, ExtendedViewHolderType> onBind(BiConsumer<ExtendedDataType, ExtendedViewHolderType> consumer) {
			bindConsumer = consumer;
			return this;
		}

		private boolean matchesItem(final ExtendedDataType item) {
			if(itemClass.isInstance(item)) {
				if (bindPredicate != null) {
					return bindPredicate.test(item);
				}
				return true;
			}
			return false;
		}

		private ExtendedViewHolderType create(final ViewGroup parent) {
			return createFunction.apply(parent);
		}

		private void bind(final ExtendedDataType card, final ExtendedViewHolderType holder) {
			if (bindConsumer != null) {
				bindConsumer.accept(card, holder);
			}
		}
	}

	private final List<Binding> mBindings = new ArrayList<>();

	/**
	 * Add a new Binding instance to this AdapterTypeBinder, making it active. Being active means that it will start
	 * being used to match data with ViewHolders.
	 * @param binding An instance of Binding.
	 * @return A self reference.
	 */
	public AdapterTypeBinder<BaseDataType, ViewHolderType> add(final Binding<? extends BaseDataType, ? extends ViewHolderType> binding) {
		mBindings.add(binding);
		return this;
	}

	/**
	 * Call this method in your adapter to have an active Binding supply you with a ViewHolder.
	 * @param parent The parent view as supplied by the Adapter's onCreateViewHolder() method.
	 * @param viewType The view type argument as supplied by the Adpater's onCreateViewHolder() method.
	 * @return  An instance of a ViewHolder built by an active Binding.
	 */
	public ViewHolderType onCreateViewHolder(final ViewGroup parent, final int viewType) {
		final Binding  binding = mBindings.get(viewType);
		return (ViewHolderType)binding.create(parent);
	}

	/**
	 * Call this method in your adapter to have an active Binding populate the supplied ViewHolder.
	 * @param holder The ViewHolder as supplied by the Adapter's onBindViewHolder() method.
	 * @param item The data item to use to populate the ViewHolder. You select your data based on the position
	 *                arg supplied by your Adapter's onBindViewHolder() method.
	 */
	public void onBindViewHolder(final ViewHolderType holder, final BaseDataType item) {
		final Binding binding = mBindings.get(getItemViewType(item));
		binding.bind(item, holder);
	}

	/**
	 * Call this method in your adapter to have an active Binding determine what your view type index is.
	 * @param item The data item to check the type for. You select your data basde on the position
	 *                arg supplied by your Adapter's getItemViewType() method.
	 * @return
	 */
	public int getItemViewType(final BaseDataType item) {
		for (int i = 0; i < mBindings.size(); i++) {
			if (mBindings.get(i).matchesItem(item)) {
				return i;
			}
		}
		return -1;
	}
}