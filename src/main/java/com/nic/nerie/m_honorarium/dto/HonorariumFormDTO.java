package com.nic.nerie.m_honorarium.dto;

import java.time.LocalDate;

public class HonorariumFormDTO {
        private Long id;
        private String program;
        private String venue;
        private LocalDate fromdate;
        private LocalDate todate;

        public HonorariumFormDTO(Long id,String program, String venue, LocalDate fromdate, LocalDate todate) {
            this.id = id;
            this.program = program;
            this.venue = venue;
            this.fromdate = fromdate;
            this.todate = todate;
        }

        public String getProgram() {
            return program;
        }

        public void setProgram(String program) {
            this.program = program;
        }

        public String getVenue() {
            return venue;
        }

        public void setVenue(String venue) {
            this.venue = venue;
        }

        public LocalDate getFromdate() {
            return fromdate;
        }

        public void setFromdate(LocalDate fromdate) {
            this.fromdate = fromdate;
        }

        public LocalDate getTodate() {
            return todate;
        }

        public void setTodate(LocalDate todate) {
            this.todate = todate;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
