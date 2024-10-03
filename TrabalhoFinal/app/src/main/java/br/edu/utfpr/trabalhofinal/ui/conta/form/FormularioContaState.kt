package br.edu.utfpr.trabalhofinal.ui.conta.form
import br.edu.utfpr.trabalhofinal.data.Conta
import br.edu.utfpr.trabalhofinal.data.TipoContaEnum
import java.time.LocalDate

data class CampoFormulario(
    val valor: String = "",
    val codigoMensagemErro: Int = 0
) {
    val contemErro get(): Boolean = codigoMensagemErro > 0
    val valido get(): Boolean = !contemErro
}
data class FormularioContaState(
    val idConta: Int = 0,
    val carregando: Boolean = false,
    val conta: Conta = Conta(),
    val erroAoCarregar: Boolean = false,
    val salvando: Boolean = false,
    val mostrarDialogConfirmacao: Boolean = false,
    val excluindo: Boolean = false,
    val contaPersistidaOuRemovida: Boolean = false,
    val codigoMensagem: Int = 0,
    val descricao: CampoFormulario = CampoFormulario(),
    val data: CampoFormulario = CampoFormulario(valor = LocalDate.now().toString()),
    val valor: CampoFormulario = CampoFormulario(),
    val paga: CampoFormulario = CampoFormulario(valor = "false"),
    val tipo: CampoFormulario = CampoFormulario(valor = TipoContaEnum.DESPESA.toString())
) {
    val contaNova get(): Boolean = idConta <= 0
    val formularioValido get(): Boolean = descricao.valido &&
            data.valido &&
            valor.valido &&
            paga.valido &&
            tipo.valido
}