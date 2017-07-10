package cn.emac.demo.spring5.components;

import cn.emac.demo.spring5.domain.Employee;
import cn.emac.demo.spring5.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EmployeeLoader implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void run(String... strings) throws Exception {
        this.employeeRepository.save(new Employee("Frodo", "Baggins", "ring bearer"));
    }
}