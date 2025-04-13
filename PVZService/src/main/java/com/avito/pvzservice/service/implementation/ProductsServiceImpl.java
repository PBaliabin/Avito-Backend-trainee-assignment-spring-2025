package com.avito.pvzservice.service.implementation;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.Product;
import com.avito.pvzservice.domain.Reception;
import com.avito.pvzservice.domain.User;
import com.avito.pvzservice.exception.BadRequestException;
import com.avito.pvzservice.exception.InternalServerErrorException;
import com.avito.pvzservice.exception.UnauthorizedException;
import com.avito.pvzservice.jwt.JwtTokenService;
import com.avito.pvzservice.model.request.AddProductRequest;
import com.avito.pvzservice.model.response.AddProductResponse;
import com.avito.pvzservice.repository.PVZRepository;
import com.avito.pvzservice.repository.ProductRepository;
import com.avito.pvzservice.repository.ReceptionRepository;
import com.avito.pvzservice.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor()
public class ProductsServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ReceptionRepository receptionRepository;
    private final PVZRepository pvzRepository;

    private JwtTokenService jwtTokenService;


    @Override
    public ResponseEntity<AddProductResponse> addProduct(AddProductRequest addProductRequest, String token) {
        if (!jwtTokenService.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired JWT token");
        }

        if (!Objects.equals(jwtTokenService.getRoleFromToken(token), String.valueOf(User.Role.employee))) {
            throw new UnauthorizedException("Invalid access role");
        }

        PVZ pvz = pvzRepository.findById(addProductRequest.getPvzId()).orElse(null);
        if (pvz == null) {
            throw new BadRequestException("PVZ not found");
        }

        Reception reception = receptionRepository.findLatestByPvzId(addProductRequest.getPvzId()).orElse(null);
        if (reception == null) {
            throw new BadRequestException("No reception found");
        }

        if (reception.getStatus() == Reception.Status.close) {
            throw new BadRequestException("No open reception found");
        }

        Product product = new Product();
        product.setType(addProductRequest.getType());

        Product saved = productRepository.save(product);
        reception.addProduct(saved);
        receptionRepository.save(reception);

        AddProductResponse addProductResponse = new AddProductResponse();
        addProductResponse.setId(saved.getUuid());
        addProductResponse.setDateTime(saved.getDateTime());
        addProductResponse.setReceptionId(reception.getUuid());
        addProductResponse.setType(saved.getType());

        return ResponseEntity.ok().body(addProductResponse);
    }
}
