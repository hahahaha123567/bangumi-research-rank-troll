import lombok.*;

/**
 * @author hahahaha123567@qq.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Anime {

	private String name;

	private String nameCN;

	private Integer rank;

	private Integer scientificRank;

	public String getTrueName() {
		if (nameCN == null || nameCN.isEmpty()) {
			return name;
		} else {
			return nameCN;
		}
	}

	public Double getCjbScore() {
		return getScientificRank() * 1.0d / getRank();
	}

	public String getDescription() {
		return String.format("%s, \tbangumi rank = %d, \tscientific rank = %d, \tcjb指数 = %f",
				getTrueName(), getRank(), getScientificRank(), getCjbScore());
	}
}
