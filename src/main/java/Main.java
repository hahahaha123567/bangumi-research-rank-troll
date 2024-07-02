import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hahahaha123567@qq.com
 */
public class Main {

	public static void main(String[] args) throws IOException {
		List<Anime> animeList = getAnime();

		rank(animeList, 10);

		System.out.println("100名内的动画");
		rank(animeList.stream().filter(anime -> anime.getRank() <= 100 || anime.getScientificRank() <= 100).collect(Collectors.toList()), 10);
	}

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static List<Anime> getAnime() throws IOException {
		String url = "https://chii.ai/graphql";
		Map<String, Object> requestData = new HashMap<>();
		requestData.put("operationName", "GetRankingList");
		Map<String, Object> variables = new HashMap<>();
		variables.put("type", "anime");
		requestData.put("variables", variables);
		requestData.put("query", "query GetRankingList($type: String!) {\n  queryRankingList(type: $type) {\n    ...SubjectSearchResult\n  }\n}\n\nfragment SubjectSearchResult on SearchResult {\n  result {\n    ... on Subject {\n      ...Subject\n    }\n   }\n }\n\nfragment Subject on Subject {\n  name\n  nameCN\n  rank\n  scientificRank\n }");
		StringEntity entity = new StringEntity(objectMapper.writeValueAsString(requestData), ContentType.APPLICATION_JSON);

		String response = Request.post(url)
				.addHeader(new BasicHeader("Content-Type", "application/json"))
				.body(entity)
				.execute()
				.returnContent()
				.asString();

		JsonNode resultNode = objectMapper.readTree(response).path("data").path("queryRankingList").get("result");
		List<Anime> animeList = new ArrayList<>();
		for (JsonNode subjectNode : resultNode) {
			animeList.add(new Anime(subjectNode.get("name").asText(), subjectNode.get("nameCN").asText(), subjectNode.get("rank").asInt(), subjectNode.get("scientificRank").asInt()));
		}
		return animeList;
	}

	private static void rank(List<Anime> animeList, int topN) {
		TreeMap<Double, Anime> map = new TreeMap<>();
		animeList.stream()
				.filter(anime -> anime.getScientificRank() != 0 && anime.getRank() != 0)
				.forEach(anime -> map.put(anime.getCjbScore(), anime));

		System.out.println("在bangumi最值得更高排名的N部动画");
		map.entrySet().stream().limit(topN).forEach(entry -> System.out.println(entry.getValue().getDescription()));
		System.out.println();

		System.out.println("在bangumi最德不配位的N部动画");
		List<Map.Entry<Double, Anime>> lastEntries = map.entrySet().stream().skip(map.size() - topN).collect(Collectors.toList());
		Collections.reverse(lastEntries);
		lastEntries.forEach(entry -> System.out.println(entry.getValue().getDescription()));
	}
}
