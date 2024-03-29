package br.com.caelum.eats.distancia;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

/*
 * Serviço que simula a obtenção dos restaurantes mais próximos a um dado CEP.
 * Deve evoluir para uma solução que utiliza geolocalização.
 * 
 */
@Service
@AllArgsConstructor
class DistanciaService {

	private static final Pageable LIMIT = PageRequest.of(0, 5);

	private RestauranteRepository restaurantes;
	private RecomendacoesGrpcClient recomendacoesClient;

	List<RestauranteComDistanciaDto> restaurantesMaisProximosAoCep(String cep) {
		List<Restaurante> aprovados = restaurantes.findAll(LIMIT).getContent();
		return calculaDistanciaParaOsRestaurantes(aprovados, cep);
	}

	List<RestauranteComDistanciaDto> restaurantesDoTipoDeCozinhaMaisProximosAoCep(Long tipoDeCozinhaId, String cep) {
		List<Restaurante> aprovadosDoTipoDeCozinha = restaurantes
				.findAllByTipoDeCozinhaId(tipoDeCozinhaId, LIMIT).getContent();
		return calculaDistanciaParaOsRestaurantes(aprovadosDoTipoDeCozinha, cep);
	}

	RestauranteComDistanciaDto restauranteComDistanciaDoCep(Long restauranteId, String cep) {
		Restaurante restaurante = restaurantes.findById(restauranteId)
				.orElseThrow(() -> new ResourceNotFoundException());
		String cepDoRestaurante = restaurante.getCep();
		BigDecimal distancia = distanciaDoCep(cepDoRestaurante, cep);
		return new RestauranteComDistanciaDto(restauranteId, distancia);
	}

	private List<RestauranteComDistanciaDto> calculaDistanciaParaOsRestaurantes(List<Restaurante> restaurantes,
			String cep) {
		return ordenaPorRecomendacoes(restaurantes).stream().map(restaurante -> {
			String cepDoRestaurante = restaurante.getCep();
			BigDecimal distancia = distanciaDoCep(cepDoRestaurante, cep);
			Long restauranteId = restaurante.getId();
			return new RestauranteComDistanciaDto(restauranteId, distancia);
		}).collect(Collectors.toList());
	}
	
	private List<Restaurante> ordenaPorRecomendacoes(List<Restaurante> restaurantes) {
		if (restaurantes.size() > 1) {
			List<Long> idsDeRestaurantes = restaurantes.stream().map(Restaurante::getId).collect(Collectors.toList());
			List<Long> idsDeRestaurantesOrdenadosPorRecomendacao = recomendacoesClient.ordenaPorRecomendacoes(idsDeRestaurantes);
			List<Restaurante> restaurantesOrdenadosPorRecomendacao = new ArrayList<>(restaurantes);
			restaurantesOrdenadosPorRecomendacao.sort(Comparator.comparing(restaurante -> idsDeRestaurantesOrdenadosPorRecomendacao.indexOf(restaurante.getId())));
			return restaurantesOrdenadosPorRecomendacao;
		}
		return restaurantes;
	}

	private BigDecimal distanciaDoCep(String cepDoRestaurante, String cep) {
		return calculaDistancia();
	}

	private BigDecimal calculaDistancia() {
		// simulaDemora();
		return new BigDecimal(Math.random() * 15);
	}

	@SuppressWarnings("unused")
	private void simulaDemora() {
		// simula demora de 10s a 20s
		long demora = (long) (Math.random() * 10000 + 10000);
		try {
			Thread.sleep(demora);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
