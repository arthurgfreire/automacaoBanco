/////////Vai ser gerado a classe completa de acordo quantas interfaces form cadastradas
//package "<nomeDoCaminhoAondeEstaSendoGerado>"
//import feign.FeignException;
//import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
//import io.github.resilience4j.retry.annotation.Retry;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.util.Collections;
//import java.util.List;
//
//@Component
//public class "<nomeinterface>primeira letraMaiuscula"GatewayImpl implements "<nomeinterface>primeira letraMaiuscula"Gateway {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger("<nomeinterface>primeira letraMaiuscula"GatewayImpl.class);
//
//    private final "<nomeinterface>primeira letraMaiuscula"Client "<nomeinterfacePrimeiraletraminuscula>"Client;
//
//    public "<nomeinterface>primeira letraMaiuscula"GatewayImpl("<nomeinterface>primeira letraMaiuscula"Client "<nomeinterfacePrimeiraletraminuscula>"Client) {
//        this."<nomeinterface>primeira letraMaiuscula"Client = "<nomeinterfacePrimeiraletraminuscula>"Client;
//    }
//
/////////// inicio 1 - O metodo será gerado de acorto o array "PARÂMETROS DO MÉTODO" pode ser gerado varios metodos obs: será 
///// usado no campo '<NomedoObjetodeRetorno>' nome do arquivo existente na variavél "Caminho completo do DTO de retorno (ex: br.com...MeuDto):"
///// sedo esse nome os caracteres que está entre a utima "\" e o "." ex: C:\Users\PropostaDto.java o nome seria PropostaDto.
//    @CircuitBreaker(name = "'<nomeinterface>primeira letraMaiuscula'Gateway", fallbackMethod = "'<nomemetodo>'Fallback") ///se foi marcado sim no "CircuitBreaker?"
//    @Retry(name = "'<nomeinterface>primeira letraMaiuscula'Gateway")///se foi marcado sim no "Retry?"
//    @Override
//    public "Tipo de retorno: Pode ser Lista = List<'<NomedoObjetodeRetorno>', Objeto Simples = '<NomedoObjetodeRetorno>' ou Void = 'void'>" "<Nome do método (0 para encerrar):>comprimeira letra minuscula"(
//    		//////// Inicio 2 -Lista de parametros do metodo obs: a "Anotação:" escolhida for "(4) @RequestBody" o 
//    		/// "<Tipo (Integer, Long, String)>" será subistituido pelo nome do arquivo existente na variavél "Caminho do DTO do Body (ex: br.com...BodyDto):"
//    		/// sedo esse nome os caracteres que está entre a utima "\" e o "." ex: C:\Users\PropostaDto o nome seria PropostaDto.
//    		"<Tipo (Integer, Long, String)>[0]" "<Nomedavariáveldoparametro>[0]",
//    		"<Tipo (Integer, Long, String)>[1]" "<Nomedavariáveldoparametro>[1]",
//    		"<caracteres que está entre a utima '\' e o '.' >[2]" "<Nomedavariáveldoparametro>[2]"
//    		////Fim 2 
//    		) {    		    			
//        LOGGER.info("Iniciando '<Nome do método (0 para encerrar):>comprimeira letra minuscula' de '<nomeinterface>': {}", proposta);
//
//        List<"<NomedoObjetodeRetorno>"> response =
//        	"<nomeinterfacePrimeiraletraminuscula>"Client."<nomemetodo>"(
//        						////////Inicio 3 - Lista de parametros do metodo
//                                "<Nomedavariáveldoparametro>[0]",
//                                "<Nomedavariáveldoparametro>[1]",
//                                "<Nomedavariáveldoparametro>[2]"
//                                ///fim 3
//                        );
//
//        if (response == null) {
//            return Collections.emptyList();
//        }
//
//        return response;
//    }
//    ///Inicio 4 - Se foi marcado sim no 'Fallback?'
//    private "Tipo de retorno: Pode ser Lista = List<'<NomedoObjetodeRetorno>', Objeto Simples = '<NomedoObjetodeRetorno>' ou Void = 'void'>" "<Nome do método (0 para encerrar):>comprimeira letra minuscula"Fallback(
//    		//////// Inicio 5 -Lista de parametros do metodo obs: a "Anotação:" escolhida for "(4) @RequestBody" o 
//    		/// "<Tipo (Integer, Long, String)>" será subistituido pelo nome do arquivo existente na variavél "Caminho do DTO do Body (ex: br.com...BodyDto):"
//    		/// sedo esse nome os caracteres que está entre a utima "\" e o "." ex: C:\Users\PropostaDto o nome seria PropostaDto.
//    		"<Tipo (Integer, Long, String)>[0]" "<Nomedavariáveldoparametro>[0]",
//    		"<Tipo (Integer, Long, String)>[1]" "<Nomedavariáveldoparametro>[1]",
//    		"<caracteres que está entre a utima '\' e o '.' >[2]" "<Nomedavariáveldoparametro>[2]",
//    	////Fim 5 
//    		Throwable t) {
//        LOGGER.warn("Fallback '<Nome do método (0 para encerrar):>comprimeira letra minuscula'. causa={}", t.toString(), t);
//        throw new RuntimeException("FallBack para o '<nomeinterface>primeira<nomeinterface>primeira letraMaiuscula'GatewayImpl");
//    }
//    //////Fim 4 
///////////Fim - 1
//}
