///////////Vai ser gerado a classe completa de acordo quantas interfaces form cadastradas
//package "<nomeDoCaminhoAondeEstaSendoGerado>"
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@FeignClient(
//        name = "'<nomeinterface>primeira letraMaiuscula'Client",
//        /// Será colocado o caminho que foi criado na URL no aplication,uml para cada inferface separado por '.' ex: bradesco.integracoes.bcaq.endereco.base-url
//        url = "${bradesco.integracoes.bcaq.'<nomeinterface>-Todo minusculo'.base-url}",
//        configuration = FeignClientConfig.class
//)
//public interface "<nomeinterface>primeira letraMaiuscula"Client {
/////////// inicio 1 - O metodo será gerado de acorto o array "PARÂMETROS DO MÉTODO" pode ser gerado varios metodos obs: será 
///// usado no campo '<NomedoObjetodeRetorno>' nome do arquivo existente na variavél "Caminho completo do DTO de retorno (ex: br.com...MeuDto):"
///// sedo esse nome os caracteres que está entre a utima "\" e o "." ex: C:\Users\PropostaDto.java o nome seria PropostaDto.
//    @GetMapping(value = "'<Path de comunicação (ex: v1/proposta ou v1/{numeroProposta}/produto):>'", produces = MediaType.APPLICATION_JSON_VALUE)
//    "Tipo de retorno: Pode ser Lista = List<'<NomedoObjetodeRetorno>', Objeto Simples = '<NomedoObjetodeRetorno>' ou Void = 'void'>" "<Nome do método (0 para encerrar):>comprimeira letra minuscula"(
//    		//////// Inicio 2 -Lista de parametros do metodo obs: a "Anotação:" escolhida for "(4) @RequestBody" o 
//       		/// "<Tipo (Integer, Long, String)>" será subistituido pelo nome do arquivo existente na variavél "Caminho do DTO do Body (ex: br.com...BodyDto):"
//       		/// sedo esse nome os caracteres que está entre a utima "\" e o "." ex: C:\Users\PropostaDto o nome seria PropostaDto.
//    		"<Anotação:>"("'<Nomedavariáveldoparametro>[0]'") "<Tipo (Integer, Long, String)>[0]" "<Nomedavariáveldoparametro>[0]",
//    		"<Anotação:>"("'<Nomedavariáveldoparametro>[0]'") String "<Tipo (Integer, Long, String)>[1]" "<Nomedavariáveldoparametro>[1]",
//    		"<Anotação:>-Caso seja (4) @RequestBody não terar '('<Nomedavariáveldoparametro>[0]')' como acrescentado nos parametros anteriores" "<caracteres que está entre a utima '\' e o '.' >[2]" "<Nomedavariáveldoparametro>[2]"
//        ////Fim 2 
//    );
///////////Fim - 1
//}