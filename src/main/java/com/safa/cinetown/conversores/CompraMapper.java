package com.safa.cinetown.conversores;

import com.safa.cinetown.dtos.CompraResponse;
import com.safa.cinetown.dtos.request.CompraRequest;
import com.safa.cinetown.modelos.Compra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompraMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "sesion", ignore = true)
    @Mapping(target = "codCompra", ignore = true)
    @Mapping(target = "fechaCompra", ignore = true)
    @Mapping(target = "precioTotal", ignore = true)
    @Mapping(target = "estado", constant = "true")
    @Mapping(target = "parkingUsado", constant = "false")
    Compra aEntidad(CompraRequest request);

    CompraResponse aRespuesta(Compra compra);
}
