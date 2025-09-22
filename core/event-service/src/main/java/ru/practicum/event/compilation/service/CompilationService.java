package ru.practicum.event.compilation.service;

import ru.practicum.event.compilation.dto.CompilationDto;
import ru.practicum.event.compilation.dto.NewCompilationDto;
import ru.practicum.event.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(GetCompilationsParam param);

    CompilationDto getCompilationById(Long compId);

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId);

    void deleteCompilation(Long compId);
}
