package ar.edu.itba.pod.agent.market;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import ar.edu.itba.pod.doc.Immutable;

/**
 * A resource description, based on a category and name
 */
@Immutable
public class Resource implements Serializable {
	private static final long serialVersionUID = -2536070234575114892L;

	private final String category;
	private final String name;

	public Resource(String category, String name) {
		checkNotNull(category, "Category cannot be null");
		checkNotNull(name, "Name cannot be null");
		this.name = name;
		this.category = category;
	}

	public String name() {
		return this.name;
	}
	
	public String category() {
		return this.category;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, category);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Resource other = (Resource) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
