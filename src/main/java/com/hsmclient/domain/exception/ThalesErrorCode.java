package com.hsmclient.domain.exception;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Catalogo de codigos de error genericos del manual Thales (Host Command Reference).
 * Al ser un enum, el compilador detecta codigos mal escritos y el catalogo queda
 * documentado en un solo lugar, sin depender de un {@code HashMap<String,String>}
 * mantenido en paralelo.
 */
public enum ThalesErrorCode {
    OK("00", "Sin error"),
    VERIFICATION_FAILURE("01", "Fallo de verificacion o paridad de llave importada"),
    KEY_LENGTH_INAPPROPRIATE("02", "Longitud de llave inapropiada para el algoritmo"),
    INVALID_KEY_TYPE_CODE("04", "Codigo de tipo de llave invalido"),
    INVALID_KEY_LENGTH_FLAG("05", "Flag de longitud de llave invalido"),
    SOURCE_KEY_PARITY_ERROR("10", "Error de paridad en la llave origen"),
    DESTINATION_KEY_PARITY_ERROR("11", "Error de paridad en la llave destino, o llave en todo ceros"),
    USER_STORAGE_NOT_AVAILABLE("12", "Contenido de almacenamiento de usuario no disponible"),
    INVALID_LMK_IDENTIFIER("13", "Identificador de LMK invalido"),
    PIN_UNDER_LMK_02_03_INVALID("14", "PIN cifrado bajo LMK 02-03 invalido"),
    INVALID_INPUT_DATA("15", "Datos de entrada invalidos (formato, caracteres o datos insuficientes)"),
    CONSOLE_OR_PRINTER_NOT_READY("16", "Consola/impresora no lista o no conectada"),
    NOT_AUTHORIZED("17", "HSM no autorizado, u operacion prohibida por configuracion de seguridad"),
    DOCUMENT_FORMAT_NOT_LOADED("18", "Definicion de formato de documento no cargada"),
    INVALID_DIEBOLD_TABLE("19", "Tabla Diebold especificada invalida"),
    INVALID_PIN_BLOCK("20", "El PIN block no contiene valores validos"),
    INVALID_INDEX("21", "Indice invalido, u overflow de bloque/indice"),
    INVALID_ACCOUNT_NUMBER("22", "Numero de cuenta invalido"),
    INVALID_PIN_BLOCK_FORMAT_CODE("23", "Codigo de formato de PIN block invalido o no permitido por PCI HSM"),
    INVALID_PIN_LENGTH("24", "El PIN tiene menos de 4 o mas de 12 digitos"),
    DECIMALIZATION_TABLE_ERROR("25", "Error en la tabla de decimalizacion"),
    INVALID_KEY_SCHEME("26", "Esquema de llave invalido"),
    INCOMPATIBLE_KEY_LENGTH("27", "Longitud de llave incompatible"),
    INVALID_KEY_TYPE("28", "Tipo de llave invalido"),
    KEY_FUNCTION_NOT_PERMITTED("29", "Funcion de llave no permitida"),
    INVALID_REFERENCE_NUMBER("30", "Numero de referencia invalido"),
    INSUFFICIENT_SOLICITATION_ENTRIES("31", "Entradas de solicitud insuficientes para el batch"),
    AES_NOT_INSTALLED("32", "Licencia AES (LIC007) no instalada"),
    LMK_KEY_CHANGE_STORAGE_CORRUPT("33", "Almacenamiento de cambio de llave LMK corrupto"),
    FRAUD_DETECTION("39", "Deteccion de fraude"),
    INVALID_CHECKSUM("40", "Checksum invalido"),
    INTERNAL_HARDWARE_SOFTWARE_ERROR("41", "Error interno de hardware/software"),
    DES_FAILURE("42", "Fallo DES"),
    RSA_KEY_GENERATION_FAILURE("43", "Fallo en generacion de llave RSA"),
    INVALID_TAG_FOR_ENCRYPTED_PIN("46", "Tag invalido para PIN cifrado"),
    ALGORITHM_NOT_LICENSED("47", "Algoritmo no licenciado"),
    PRIVATE_KEY_ERROR("49", "Error de llave privada, reportar al supervisor"),
    INVALID_MESSAGE_HEADER("51", "Header de mensaje invalido"),
    TRANSACTION_KEY_SCHEME_NONE("65", "Esquema de llave de transaccion configurado en None"),
    COMMAND_NOT_LICENSED("67", "Comando no licenciado"),
    COMMAND_DISABLED("68", "Comando deshabilitado"),
    PIN_BLOCK_FORMAT_DISABLED("69", "Formato de PIN block deshabilitado"),
    INVALID_DIGEST_INFO_SYNTAX("74", "Sintaxis de digest info invalida"),
    SINGLE_LENGTH_KEY_MASQUERADING("75", "Llave simple longitud disfrazada de doble/triple"),
    RSA_KEY_LENGTH_ERROR("76", "Error de longitud de llave RSA o datos cifrados RSA"),
    CLEAR_DATA_BLOCK_ERROR("77", "Error en bloque de datos en claro"),
    PRIVATE_KEY_LENGTH_ERROR("78", "Error de longitud de llave privada"),
    HASH_ALGORITHM_OID_ERROR("79", "Error de object identifier de algoritmo de hash"),
    DATA_LENGTH_ERROR("80", "Error de longitud de datos"),
    INVALID_CERTIFICATE_HEADER("81", "Header de certificado invalido"),
    INVALID_CHECK_VALUE_LENGTH("82", "Longitud de check value invalida"),
    KEY_BLOCK_FORMAT_ERROR("83", "Error de formato de key block"),
    KEY_BLOCK_CHECK_VALUE_ERROR("84", "Error de check value del key block"),
    INVALID_OAEP_MGF("85", "Mask Generation Function OAEP invalida"),
    INVALID_OAEP_MGF_HASH("86", "Hash function OAEP MGF invalida"),
    OAEP_PARAMETER_ERROR("87", "Error de parametro OAEP"),
    DATA_PARITY_ERROR("90", "Error de paridad de datos en el mensaje recibido"),
    LRC_MISMATCH("91", "LRC no coincide con el valor calculado"),
    COUNT_VALUE_INVALID("92", "Valor de Count invalido o fuera de limites"),
    INCOMPATIBLE_LMK_SCHEMES("A1", "Esquemas de LMK incompatibles"),
    INCOMPATIBLE_LMK_IDENTIFIERS("A2", "Identificadores de LMK incompatibles"),
    INCOMPATIBLE_KEY_BLOCK_LMK_IDS("A3", "Identificadores de LMK de key block incompatibles"),
    KEY_BLOCK_AUTH_FAILURE("A4", "Fallo de autenticacion del key block"),
    INCOMPATIBLE_KEY_LENGTH_A5("A5", "Longitud de llave incompatible"),
    INVALID_KEY_USAGE("A6", "Uso de llave invalido"),
    INVALID_ALGORITHM("A7", "Algoritmo invalido"),
    INVALID_MODE_OF_USE("A8", "Modo de uso invalido"),
    INVALID_KEY_VERSION_NUMBER("A9", "Numero de version de llave invalido"),
    INVALID_EXPORT_FIELD("AA", "Campo de exportacion invalido"),
    INVALID_NUMBER_OF_OPTIONAL_BLOCKS("AB", "Numero de bloques opcionales invalido"),
    OPTIONAL_HEADER_BLOCK_ERROR("AC", "Error en bloque de header opcional"),
    KEY_STATUS_OPTIONAL_BLOCK_ERROR("AD", "Error en bloque opcional de estado de llave"),
    INVALID_START_DATE("AE", "Fecha/hora de inicio invalida"),
    INVALID_END_DATE("AF", "Fecha/hora de fin invalida"),
    INVALID_ENCRYPTION_MODE("B0", "Modo de cifrado invalido"),
    INVALID_AUTHENTICATION_MODE("B1", "Modo de autenticacion invalido"),
    MISC_KEY_BLOCK_ERROR("B2", "Error miscelaneo de key block"),
    INVALID_NUMBER_OF_OPTIONAL_BLOCKS_B3("B3", "Numero de bloques opcionales invalido"),
    OPTIONAL_BLOCK_DATA_ERROR("B4", "Error de datos en bloque opcional"),
    INCOMPATIBLE_COMPONENTS("B5", "Componentes incompatibles"),
    INCOMPATIBLE_KEY_STATUS_BLOCKS("B6", "Bloques opcionales de estado de llave incompatibles"),
    INVALID_CHANGE_FIELD("B7", "Campo de cambio invalido"),
    INVALID_OLD_VALUE("B8", "Valor anterior invalido"),
    INVALID_NEW_VALUE("B9", "Valor nuevo invalido"),
    NO_KEY_STATUS_BLOCK("BA", "No hay bloque de estado de llave en el key block"),
    INVALID_WRAPPING_KEY("BB", "Llave de wrapping invalida"),
    REPEATED_OPTIONAL_BLOCK("BC", "Bloque opcional repetido"),
    INCOMPATIBLE_KEY_TYPES("BD", "Tipos de llave incompatibles"),
    INVALID_KEY_BLOCK_HEADER_ID("BE", "ID de header de key block invalido"),
    UNKNOWN("??", "Codigo de error no reconocido por este cliente");

    private static final Map<String, ThalesErrorCode> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toMap(ThalesErrorCode::code, e -> e));

    private final String code;
    private final String description;

    ThalesErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public boolean isSuccess() {
        return this == OK;
    }

    public static ThalesErrorCode fromCode(String code) {
        return BY_CODE.getOrDefault(code, UNKNOWN);
    }
}
