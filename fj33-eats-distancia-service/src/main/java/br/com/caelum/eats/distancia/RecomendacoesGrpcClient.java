package br.com.caelum.eats.distancia;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.caelum.eats.recomendacoes.grpc.Recomendacoes.Restaurantes;
import br.com.caelum.eats.recomendacoes.grpc.RecomendacoesDeRestaurantesGrpc;
import br.com.caelum.eats.recomendacoes.grpc.RecomendacoesDeRestaurantesGrpc.RecomendacoesDeRestaurantesBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
class RecomendacoesGrpcClient {

	private ManagedChannel channel;
	private RecomendacoesDeRestaurantesBlockingStub recomendacoes;
	
	private String recomendacoesServiceHost;
	private Integer recomendacoesServicePort;

	RecomendacoesGrpcClient(@Value("${recomendacoes.service.host}") String recomendacoesServiceHost, 
							@Value("${recomendacoes.service.port}") Integer recomendacoesServicePort) {
		this.recomendacoesServiceHost = recomendacoesServiceHost;
		this.recomendacoesServicePort = recomendacoesServicePort;
	}

	@PostConstruct
	void conectaAoRecomendacoesGrpcService() {
		channel = ManagedChannelBuilder.forAddress(recomendacoesServiceHost, recomendacoesServicePort)
		          .usePlaintext() // desabilita TLS pq precisa de certificado (apenas para testes)
		          .build();
		recomendacoes = RecomendacoesDeRestaurantesGrpc.newBlockingStub(channel);
	}

	List<Long> ordenaPorRecomendacoes(List<Long> idsDeRestaurantes) {
		Restaurantes restaurantes = Restaurantes.newBuilder().addAllRestauranteId(idsDeRestaurantes).build();
		
		Restaurantes restaurantesOrdenadosPorRecomendacao = recomendacoes.recomendacoes(restaurantes);
		
		List<Long> restaurantesOrdenados = restaurantesOrdenadosPorRecomendacao.getRestauranteIdList();
		
		log.info("Restaurantes ordenados: {}", restaurantesOrdenados);
		
		return restaurantesOrdenados;
	}
	
	@PreDestroy
	void desconectaDoRecomendacoesGrpcService() {
		channel.shutdown();
	}
}
