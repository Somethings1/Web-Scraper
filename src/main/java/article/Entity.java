package article;

import java.util.Objects;

/**
 * Entity detected by Stanford NER library
*/
public class Entity {
	public String type;
	public String content;
	
	public Entity (String type, String content) {
		this.type = type;
		this.content = content;
	}

	@Override
	public int hashCode() {
		return Objects.hash(content, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		return Objects.equals(content, other.content) && Objects.equals(type, other.type);
	}
}