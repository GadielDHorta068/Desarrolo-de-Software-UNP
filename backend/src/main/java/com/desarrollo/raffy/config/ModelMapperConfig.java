package com.desarrollo.raffy.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.modelmapper.Converter;

import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.dto.EventSummaryDTO;
import com.desarrollo.raffy.dto.GiveawaysDTO;
import com.desarrollo.raffy.dto.GuessingContestDTO;
import com.desarrollo.raffy.dto.CreatorSummaryDTO;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.GuessingContest;

/**
 * Configuración de ModelMapper para el mapeo automático entre entidades y DTOs
 * 
 * ModelMapper facilita la conversión entre objetos de diferentes capas:
 * - Entidades (modelo de datos) → DTOs (para APIs REST)
 * - DTOs → Entidades (para persistencia en BD)
 * - ViewModels (objetos optimizados para la vista)
 * 
 * CÓMO USAR MODELMAPPER EN TUS SERVICIOS:
 * 
 * 1. Inyectar ModelMapper en tu servicio:
 *    @Autowired
 *    private ModelMapper modelMapper;
 * 
 * 2. Convertir Entidad a DTO:
 *    UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
 * 
 * 3. Convertir DTO a Entidad:
 *    Users userEntity = modelMapper.map(userDTO, Users.class);
 * 
 * 4. Mapear listas:
 *    List<UserDTO> userDTOs = users.stream()
 *        .map(user -> modelMapper.map(user, UserDTO.class))
 *        .collect(Collectors.toList());
 * 
 * EJEMPLO PRÁCTICO EN UN CONTROLADOR:
 * 
 * @RestController
 * public class UserController {
 *     @Autowired
 *     private ModelMapper modelMapper;
 *     
 *     @GetMapping("/users/{id}")
 *     public UserDTO getUser(@PathVariable Long id) {
 *         Users user = userService.findById(id);
 *         return modelMapper.map(user, UserDTO.class);
 *     }
 * }
 * 
 * CONFIGURACIÓN ACTUAL:
 * - STRICT: Solo mapea campos con nombres exactamente iguales
 * - Field Matching: Permite mapear campos privados directamente
 * - Private Access: Accede a campos privados sin necesidad de getters/setters
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Bean de ModelMapper configurado para el proyecto
     * 
     * Configuración optimizada para:
     * - Mapeo seguro (STRICT matching)
     * - Acceso a campos privados
     * - Compatibilidad con Lombok (@Data, @Getter, @Setter)
     * 
     * @return ModelMapper configurado y listo para usar
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // NOTA: Para mapeos personalizados complejos, agregar configuraciones aquí:
        // mapper.createTypeMap(SourceClass.class, DestinationClass.class)
        //       .addMapping(src -> src.getField(), DestinationClass::setMappedField);

        // Configuración específica para RegisteredUserDTO -> RegisteredUser
        mapper.createTypeMap(com.desarrollo.raffy.dto.RegisteredUserDTO.class, com.desarrollo.raffy.model.RegisteredUser.class)
                .setProvider(request -> {
                    com.desarrollo.raffy.dto.RegisteredUserDTO dto = (com.desarrollo.raffy.dto.RegisteredUserDTO) request.getSource();
                    return new com.desarrollo.raffy.model.RegisteredUser(
                            dto.getName(),
                            dto.getSurname(), 
                            dto.getEmail(),
                            dto.getCellphone(),
                            dto.getNickname(),
                            dto.getPassword()
                    );
                });

        // RegisteredUser -> CreatorSummaryDTO
        mapper.createTypeMap(RegisteredUser.class, CreatorSummaryDTO.class);

        // Converters para aplanar category en EventSummaryDTO
        Converter<Events, Long> categoryIdConverter = ctx -> {
            Events src = ctx.getSource();
            return (src != null && src.getCategory() != null) ? src.getCategory().getId() : null;
        };
        Converter<Events, String> categoryNameConverter = ctx -> {
            Events src = ctx.getSource();
            return (src != null && src.getCategory() != null) ? src.getCategory().getName() : null;
        };

        // Events -> EventSummaryDTO (incluye mapeo de creator usando el typeMap anterior y category aplanado)
        mapper.createTypeMap(Events.class, EventSummaryDTO.class)
              .addMappings(m -> {
                  m.using(categoryIdConverter).map(src -> src, EventSummaryDTO::setCategoryId);
                  m.using(categoryNameConverter).map(src -> src, EventSummaryDTO::setCategoryName);
                  // creator se mapea automáticamente usando el typeMap RegisteredUser -> CreatorSummaryDTO
              });

        // Giveaways -> EventSummaryDTO (asegura que los sorteos también mapeen categoryId/categoryName)
        mapper.createTypeMap(Giveaways.class, EventSummaryDTO.class)
              .addMappings(m -> {
                  m.using(categoryIdConverter).map(src -> src, EventSummaryDTO::setCategoryId);
                  m.using(categoryNameConverter).map(src -> src, EventSummaryDTO::setCategoryName);
              });

        // Mapeo polimórfico para Events -> EventSummaryDTO según la subclase
        mapper.typeMap(Events.class, EventSummaryDTO.class)
              .setProvider(request -> {
                    Events src = (Events) request.getSource();
                    if(src instanceof Giveaways) return new GiveawaysDTO();
                    if(src instanceof GuessingContest) return new GuessingContestDTO();
                    return new EventSummaryDTO();
                });

        return mapper;
    }
    
    /*
     * TIPS PARA EL EQUIPO:
     * 
     * 1. CREAR DTOs ESPECÍFICOS:
     *    - UserCreateDTO (para crear usuarios)
     *    - UserUpdateDTO (para actualizar usuarios)
     *    - UserResponseDTO (para respuestas de API)
     * 
     * 2. VALIDAR DTOS:
     *    - Usar @Valid en controladores
     *    - Agregar @NotNull, @Size, @Email en DTOs
     * 
     * 3. MANTENER MAPEO EN SERVICIOS:
     *    - No mapear en controladores directamente
     *    - Crear métodos helper en servicios para mapeo
     * 
     * 4. TESTEAR MAPEOS:
     *    - Crear tests unitarios para verificar mapeos
     *    - Verificar que todos los campos se mapeen correctamente
     * 
     * 5. PERFORMANCE:
     *    - ModelMapper es thread-safe, se puede reutilizar
     *    - Para mapeos frecuentes, considerar cachear TypeMaps
     */
}